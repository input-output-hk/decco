package io.iohk.decco
package test.instances

import io.iohk.decco.test.utils.CodecTestingHelpers._
import io.iohk.decco.auto._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FlatSpec
import scala.reflect.ClassTag

class ProductInstancesSpec extends FlatSpec {
  behavior of "Product instances"

  sealed trait TrafficLight
  case class Red() extends TrafficLight
  case class Amber() extends TrafficLight
  case object Green extends TrafficLight

  implicit val arbitraryRedLight: Arbitrary[Red] = Arbitrary(Gen.const(Red()))
  implicit val arbitraryAmberLight: Arbitrary[Amber] = Arbitrary(Gen.const(Amber()))
  implicit val arbitraryGreenLight: Arbitrary[Green.type] = Arbitrary(Gen.const(Green))

  implicit val arbitraryTrafficLight: Arbitrary[TrafficLight] = Arbitrary(
    Gen.oneOf(Red(), Amber(), Green)
  )

  they should "support sealed trait hierarchies" in {
    testCodec[Red]
    testCodec[Amber]
    testCodec[Green.type]
    testCodec[TrafficLight]
    testCodec[TrafficLight, Red]
    testCodec[TrafficLight, Amber]
    testCodec[TrafficLight, Green.type]
  }

  def testsFor[A: Arbitrary: CodecContract: ClassTag]: Unit = {
    val aName = implicitly[ClassTag[A]].toString
    they should ("support Option[" + aName + "]") in {
      testCodec[Option[A]]
    }

    def combinedWith[B: Arbitrary: CodecContract: ClassTag]: Unit = {
      val bName = implicitly[ClassTag[B]].toString
      they should ("support Either[" + aName + ", " + bName + "]") in {
        testCodec[Either[A, B]]
      }

      they should ("support tuples (" + aName + ", " + bName + ")") in {
        testCodec[(A, B)]
        testCodec[(A, B, Int)]
        testCodec[(A, B, Int, Double)]
        testCodec[(A, B, Int, Double, Boolean)]
        testCodec[(A, B, Int, Double, Boolean, TrafficLight)]
      }
    }

    combinedWith[String]
    combinedWith[Int]
    combinedWith[TrafficLight]
    combinedWith[Red]
    combinedWith[(Int, String)]
    combinedWith[Boolean]

  }

  testsFor[String]
  testsFor[Int]
  testsFor[TrafficLight]
  testsFor[Red]
  testsFor[(Int, String)]

}
