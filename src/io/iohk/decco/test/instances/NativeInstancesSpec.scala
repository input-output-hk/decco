package io.iohk.decco.instances

import org.scalatest.FlatSpec
import io.iohk.decco.TestingHelpers._
import NativeInstances._

class NativeInstancesSpec extends FlatSpec {

  behavior of "NativeInstances"

  they should "encode and decode properly" in {
    partialCodecTest[Boolean]
    partialCodecTest[Byte]
    partialCodecTest[Short]
    partialCodecTest[Int]
    partialCodecTest[Char]
    partialCodecTest[Long]
    partialCodecTest[Float]
    partialCodecTest[Double]
  }
}
