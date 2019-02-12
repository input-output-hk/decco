package io.iohk.decco.instances

import org.scalatest.FlatSpec
import io.iohk.decco.TestingHelpers._
import NativeInstances._

class NativeInstancesSpec extends FlatSpec {

  behavior of "NativeInstances"

  they should "encode and decode properly" in {
    partialCodecTest[String]
    partialCodecTest[Boolean]
    partialCodecTest[Byte]
    partialCodecTest[Short]
    partialCodecTest[Int]
    partialCodecTest[Char]
    partialCodecTest[Long]
    partialCodecTest[Float]
    partialCodecTest[Double]
    partialCodecTest[Array[Byte]]
    partialCodecTest[Array[Short]]
    partialCodecTest[Array[Char]]
    partialCodecTest[Array[Int]]
    partialCodecTest[Array[Long]]
    partialCodecTest[Array[Float]]
    partialCodecTest[Array[Double]]
    partialCodecTest[Array[Boolean]]
  }
}
