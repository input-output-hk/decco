package io.iohk.decco

import java.nio.ByteBuffer
import java.util.UUID

import io.iohk.decco.Codec.heapCodec
import io.iohk.decco.DecodeFailure.BodyWrongType
import io.iohk.decco.TypeCode.genTypeCode
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import io.iohk.decco.auto._
import org.scalatest.mockito.MockitoSugar._
import org.scalatest.EitherValues._
import org.mockito.Mockito.{never, verify}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary._
import org.scalatest.prop.GeneratorDrivenPropertyChecks._

class CodecSpec extends FlatSpec {

  behavior of "Codecs"

  case class A(s: String, i: Int, l: List[String], u: UUID, f: Float)

  case class B(s: String, i: Int, l: List[String], u: UUID, f: Float)

  case class Wrap[T](t: T)

  sealed trait Base[T]

  case class S1[T](t: T) extends Base[T]

  case class S2[T]() extends Base[T]

  implicit val arbitraryA: Arbitrary[A] = Arbitrary(
    for {
      s <- arbitrary[String]
      i <- arbitrary[Int]
      l <- arbitrary[List[String]]
      u <- arbitrary[UUID]
      f <- arbitrary[Float]
    } yield A(s, i, l, u, f)
  )

  implicit val arbitraryWrap: Arbitrary[Wrap[A]] = Arbitrary(arbitraryA.arbitrary.map(a => Wrap(a)))

  they should "work for fixed width types" in {
    codecTest(heapCodec[Int])
  }

  they should "work for variable width types" in {
    codecTest(heapCodec[String])
  }

  they should "work for collection types" in {
    codecTest(heapCodec[List[String]])
  }

  they should "work for user types" in {
    codecTest(heapCodec[A])
  }

  they should "rehydrate type information from a buffer" in {
    forAll { message: Wrap[A] =>
      val messageCodec = heapCodec[Wrap[A]]
      val buffer: ByteBuffer = messageCodec.encode(message)

      val expectedPc = mock[PartialCodec[Wrap[A]]]
      val unexpectedPc = mock[PartialCodec[String]]
      val availableCodecs = Map[String, (Int, ByteBuffer) => Unit](
        Codec[String].typeCode.id -> messageWrapper(unexpectedPc),
        Codec[Wrap[A]].typeCode.id -> messageWrapper(expectedPc)
      )

      Codec.decodeFrame(availableCodecs, 0, buffer)

      verify(expectedPc).decode(meq(20), meq(buffer))
      verify(unexpectedPc, never()).decode(any(), any())
    }
  }

  they should "not allow TypeTag implicits to propagate everywhere -- case classes" in {

    def functionInTheNetwork[T](t: T)(implicit ev: Codec[T]): T = {
      implicit val wtt: TypeCode[Wrap[T]] = genTypeCode[Wrap, T]
      implicit val tpc: PartialCodec[T] = ev.partialCodec
      val framePf: PartialCodec[Wrap[T]] = PartialCodec[Wrap[T]]
      val frameCodec = heapCodec(framePf, wtt)

      val arr = frameCodec.encode(Wrap(t))

      val maybeRestoredFrame = frameCodec.decode(arr)

      maybeRestoredFrame.right.value.t
    }

    val a = A("string", 1, List(), UUID.randomUUID(), 1.1f)
    functionInTheNetwork(a) shouldBe a
  }

  they should "not allow TypeTag implicits to propagate everywhere -- sealed traits" in {

    def functionInTheNetwork[T](t: T)(implicit ev: Codec[T]): Base[T] = {
      implicit val wtt: TypeCode[Base[T]] = genTypeCode[Base, T]
      implicit val tpc: PartialCodec[T] = ev.partialCodec
      val framePf: PartialCodec[Base[T]] = PartialCodec[Base[T]]
      val frameCodec = heapCodec(framePf, wtt)

      val arr = frameCodec.encode(S1(t))

      val maybeRestoredFrame = frameCodec.decode(arr)

      maybeRestoredFrame.right.value
    }

    val a = A("string", 1, List(), UUID.randomUUID(), 1.1f)
    functionInTheNetwork(a) shouldBe S1(a)
  }

  they should "support the recovery of backing arrays with heap codec" in {
    val codec = heapCodec[String]
    val bytes = codec.encode("string")

    val backingArray: Array[Byte] = bytes.array()

    codec.decode(ByteBuffer.wrap(backingArray)) shouldBe Right("string")
  }

  they should "have distinct type codes for structurally equivalent types" in {
    val aCodec = heapCodec[A]
    val bCodec = heapCodec[B]
    val a = A("a", 1, List("a"), UUID.randomUUID(), 3.14f)

    val aBuffer = aCodec.encode(a)

    bCodec.decode(aBuffer).left.value shouldBe BodyWrongType(
      expectedType = MD5("CodecSpec.this.B"),
      encounteredType = MD5("CodecSpec.this.A")
    )
  }

  private def codecTest[T](codec: Codec[T])(implicit ev: Arbitrary[T]): Unit = {
    encodeDecodeTest(codec)
    corruptTypeHeaderTest(codec)
    truncatedBodyTest(codec)
    emptyBufferTest(codec)
  }

  private def encodeDecodeTest[T](codec: Codec[T])(implicit ev: Arbitrary[T]): Unit = {
    forAll { t: T =>
      codec.decode(codec.encode(t)) shouldBe Right(t)
    }
  }

  private def corruptTypeHeaderTest[T](codec: Codec[T])(implicit ev: Arbitrary[T]): Unit = {
    forAll { t: T =>
      val bytes: ByteBuffer = codec.encode(t)
      bytes.put(7, 0) // corrupt the header's type field
      codec.decode(bytes).left.value shouldBe a[DecodeFailure.BodyWrongType]
    }
  }

  private def truncatedBodyTest[T](codec: Codec[T])(implicit ev: Arbitrary[T]): Unit = {
    forAll { t: T =>
      val bytes: ByteBuffer = codec.encode(t)
      val truncatedBytes: ByteBuffer = truncateBody(bytes)
      codec.decode(truncatedBytes).left.value shouldBe a[DecodeFailure.BodyTooShort]
    }
  }

  private def emptyBufferTest[T](codec: Codec[T])(implicit ev: Arbitrary[T]): Unit = {
    codec.decode(ByteBuffer.allocate(0)).left.value shouldBe DecodeFailure.HeaderWrongFormat
  }

  private def truncateBody(bytes: ByteBuffer): ByteBuffer = {
    val (bodySize, bodyType) = Codec.headerCodec.decode(0, bytes).right.value.decoded
    val headerSize = Codec.headerCodec.size((bodySize, bodyType))
    val truncatedBytes = new Array[Byte](headerSize) // just the header size
    Array.copy(bytes.array(), 0, truncatedBytes, 0, headerSize)
    ByteBuffer.wrap(truncatedBytes)
  }

  private def messageWrapper[T](pf: PartialCodec[T])(start: Int, source: ByteBuffer): Unit =
    pf.decode(start, source)
}
