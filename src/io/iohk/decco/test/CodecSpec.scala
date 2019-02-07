package io.iohk.decco

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import io.iohk.decco.auto._

class CodecSpec extends FlatSpec {

  behavior of "Codecs"

  they should "encode/decode a byte" in {
    val codec = Codec[Byte]
    codec.decode(codec.encode(0)) shouldBe Some(0)
  }

  they should "handle nasty arrays coming off the network" in {

  }

  case class A(s: String)

  case class Frame[T](t: T)

  they should "not allow TypeTag implicits to propagate everywhere" in {

    def functionInTheNetwork[T: PartialCodec](t: T): T = {
      val framePf: PartialCodec[Frame[T]] = PartialCodec[Frame[T]]

      val frameCodec = Codec(framePf)

      val arr = frameCodec.encode(Frame(t))

      val maybeRestoredFrame = frameCodec.decode(arr)

      maybeRestoredFrame.get.t
    }

    functionInTheNetwork(A("string")) shouldBe "string"
  }
}
