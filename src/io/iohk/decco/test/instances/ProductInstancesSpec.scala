package io.iohk.decco.instances

import io.iohk.decco.TestingHelpers.partialCodecTest
import io.iohk.decco.auto._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FlatSpec

class ProductInstancesSpec extends FlatSpec {
  behavior of "Product instances"

  sealed trait TrafficLight
  case class Red() extends TrafficLight
  case class Amber() extends TrafficLight
  case class Green() extends TrafficLight

  implicit val arbitraryTrafficLight: Arbitrary[TrafficLight] = Arbitrary(
    Gen.oneOf(Red(), Amber(), Green())
  )

  they should "support sealed trait hierarchies" in {
    partialCodecTest[TrafficLight]
  }

  they should "support option" in {
    partialCodecTest[Option[String]]
  }

  they should "support Either" in {
    partialCodecTest[Either[String, Int]]
  }
}
