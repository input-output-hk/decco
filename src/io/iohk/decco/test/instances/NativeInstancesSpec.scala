package io.iohk.decco
package test.instances

import org.scalatest.FlatSpec
import io.iohk.decco.test.utils.CodecTestingHelpers._
import auto.instances.NativeInstances._

class NativeInstancesSpec extends FlatSpec {

  behavior of "NativeInstances"

  they should "encode and decode properly" in {
    testCodec[Boolean]
    testCodec[Byte]
    testCodec[Short]
    testCodec[Int]
    testCodec[Char]
    testCodec[Long]
    testCodec[Float]
    testCodec[Double]
  }

  they should "encode and decode native array types" in {
    testCodec[Array[Byte]]
    testCodec[Array[Short]]
    testCodec[Array[Char]]
    testCodec[Array[Int]]
    testCodec[Array[Long]]
    testCodec[Array[Float]]
    testCodec[Array[Double]]
    testCodec[Array[Boolean]]
  }

}
