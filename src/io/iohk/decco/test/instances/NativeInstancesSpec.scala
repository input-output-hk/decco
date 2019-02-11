package io.iohk.decco.instances

import org.scalatest.FlatSpec
import io.iohk.decco.TestingHelpers._
import NativeInstances._

class NativeInstancesSpec extends FlatSpec {

  behavior of "NativeInstances"

  they should "encode and decode properly" in {
    encodeDecodeTest[String]
    encodeDecodeTest[Boolean]
    encodeDecodeTest[Byte]
    encodeDecodeTest[Short]
    encodeDecodeTest[Int]
    encodeDecodeTest[Char]
    encodeDecodeTest[Long]
    encodeDecodeTest[Float]
    encodeDecodeTest[Double]
    encodeDecodeTest[Array[Byte]]
    encodeDecodeTest[Array[Short]]
    encodeDecodeTest[Array[Char]]
    encodeDecodeTest[Array[Int]]
    encodeDecodeTest[Array[Long]]
    encodeDecodeTest[Array[Float]]
    encodeDecodeTest[Array[Double]]
    encodeDecodeTest[Array[Boolean]]
  }

  they should "reject truncated buffers" in {
    emptyBufferTest[String]
    emptyBufferTest[Boolean]
    emptyBufferTest[Byte]
    emptyBufferTest[Short]
    emptyBufferTest[Int]
    emptyBufferTest[Char]
    emptyBufferTest[Long]
    emptyBufferTest[Float]
    emptyBufferTest[Double]
    emptyBufferTest[Array[Byte]]
    emptyBufferTest[Array[Short]]
    emptyBufferTest[Array[Char]]
    emptyBufferTest[Array[Int]]
    emptyBufferTest[Array[Long]]
    emptyBufferTest[Array[Float]]
    emptyBufferTest[Array[Double]]
    emptyBufferTest[Array[Boolean]]
  }
}
