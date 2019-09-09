package io.iohk.decco.snappy

import io.iohk.decco.test.utils.CodecTestingHelpers._
import io.iohk.decco.definitions._
import io.iohk.decco.snappy.auto._
import org.scalatest.{FlatSpec, Matchers}
import io.iohk.decco.BufferInstantiator.global.HeapByteBuffer

class SnappySpec extends FlatSpec with Matchers {
  behavior of "snappy"

  it should "encode data properly" in {
    testCodec[Array[Byte]]
    testCodec[String]
  }

  it should "compress message" in {
    val compressableString = (1 to 100).map(_ => "foo bar").mkString("start", " ", "end")

    val naiveCodec = io.iohk.decco.auto.codecContract2Codec[String]
    val snappyCodec = io.iohk.decco.snappy.auto.snappy[String]

    snappyCodec.encode(compressableString).array().size should be < naiveCodec.encode(compressableString).array().size
  }
}
