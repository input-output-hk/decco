package io.iohk.decco

import org.scalatest.FlatSpec
import io.iohk.decco.auto._

class CodecSpec extends FlatSpec {

  behavior of "Codecs"

  they should "work" in {
    val x = BytePartialCodec
    println(x)
  }
}
