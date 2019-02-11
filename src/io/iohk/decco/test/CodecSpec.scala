package io.iohk.decco

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import io.iohk.decco.auto._
import org.scalatest.mockito.MockitoSugar._
import org.scalatest.EitherValues._
import org.mockito.Mockito.{never, verify}
import org.mockito.ArgumentMatchers.any

class CodecSpec extends FlatSpec {

  behavior of "Codecs"

  they should "encode/decode a byte" in {
    val codec = Codec[Byte]
    codec.decode(codec.encode(0)) shouldBe Right(0)
  }

  they should "encode/decode a String" in {
    val codec = Codec[String]
    val bytes = codec.encode("string")
    codec.decode(bytes) shouldBe Right("string")
  }

  they should "reject incorrectly typed buffers with the correct error" in {
    val codec = Codec[String]
    val bytes: Array[Byte] = codec.encode("a message")
    bytes(7) = 0 // corrupt the header's type field

    codec.decode(bytes).left.value shouldBe Codec.BodyWrongType
  }

  they should "reject incorrectly size buffers with the correct error" in {
    val codec = Codec[String]
    val bytes: Array[Byte] = codec.encode("a message")
    val truncatedBytes: Array[Byte] = truncateBody(bytes)

    codec.decode(truncatedBytes).left.value shouldBe Codec.BodyTooShort
  }

  private def truncateBody(bytes: Array[Byte]): Array[Byte] = {
    val (bodySize, bodyType) = Codec.headerPf.decode(0, bytes).right.value.decoded
    val headerSize = Codec.headerPf.size((bodySize, bodyType))
    val truncatedBytes = new Array[Byte](headerSize) // just the header size
    Array.copy(bytes, 0, truncatedBytes, 0, headerSize)
    truncatedBytes
  }

  they should "reject improperly formatted headers with the correct error" in {
    Codec[String].decode(new Array[Byte](0)).left.value shouldBe Codec.HeaderWrongFormat
  }

  case class A(s: String)

  case class Wrap[T](t: T)


  they should "rehydrate type information from a buffer" in {

    val message = Wrap(A("message"))
    val messageCodec = Codec[Wrap[A]]
    val buffer: Array[Byte] = messageCodec.encode(message)
    val expectedPf = mock[PartialCodec[Wrap[A]]]
    val unexpectedPf = mock[PartialCodec[String]]
    val availableCodecs = Map[String, (Int, Array[Byte]) => Unit](
      PartialCodec[String].typeCode -> messageWrapper(unexpectedPf),
      PartialCodec[Wrap[A]].typeCode -> messageWrapper(expectedPf)
    )

    Codec.decodeFrame(availableCodecs, 0, buffer)

    verify(expectedPf).decode(133, buffer)
    verify(unexpectedPf, never()).decode(any(), any())
  }

  they should "not allow TypeTag implicits to propagate everywhere" in {

    def functionInTheNetwork[T: PartialCodec](t: T): T = {
      val framePf: PartialCodec[Wrap[T]] = PartialCodec[Wrap[T]]
      val frameCodec = Codec(framePf)

      val arr = frameCodec.encode(Wrap(t))

      val maybeRestoredFrame = frameCodec.decode(arr)

      maybeRestoredFrame.right.value.t
    }

    functionInTheNetwork(A("string")) shouldBe A("string")
  }

  private def messageWrapper[T](pf: PartialCodec[T])(start: Int, source: Array[Byte]): Unit =
    pf.decode(start, source)
}
