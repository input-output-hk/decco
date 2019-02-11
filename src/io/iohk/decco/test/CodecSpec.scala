package io.iohk.decco

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import io.iohk.decco.auto._
import org.scalatest.mockito.MockitoSugar._
import org.mockito.Mockito.{verify, never}
import org.mockito.ArgumentMatchers.any

class CodecSpec extends FlatSpec {

  behavior of "Codecs"

  they should "encode/decode a byte" in {
    val codec = Codec[Byte]
    codec.decode(codec.encode(0)) shouldBe Some(0)
  }

  they should "encode/decode a String" in {
    val codec = Codec[String]
    val bytes = codec.encode("string")
    codec.decode(bytes) shouldBe Some("string")
  }

  //
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

  def messageWrapper[T](pf: PartialCodec[T])(start: Int, source: Array[Byte]): Unit =
    pf.decode(start, source)

  they should "should encode A" in {

    val aPf: PartialCodec[A] = PartialCodec[A]
    val ac = Codec(aPf)
    val b: Array[Byte] = ac.encode(A("string"))
    println(b)
  }

  they should "not allow TypeTag implicits to propagate everywhere" in {

    def functionInTheNetwork[T: PartialCodec](t: T): T = {
      val framePf: PartialCodec[Wrap[T]] = PartialCodec[Wrap[T]]
      val frameCodec = Codec(framePf)

      val arr = frameCodec.encode(Wrap(t))

      val maybeRestoredFrame = frameCodec.decode(arr)

      maybeRestoredFrame.get.t
    }

    functionInTheNetwork(A("string")) shouldBe A("string")
  }
}
