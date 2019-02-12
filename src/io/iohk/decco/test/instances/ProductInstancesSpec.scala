package io.iohk.decco.instances

import io.iohk.decco.PartialCodec
import io.iohk.decco.auto._
import org.scalatest.FlatSpec
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.EitherValues._
import org.scalatest.Matchers._

class ProductInstancesSpec extends FlatSpec {
  behavior of "Product instances"

  sealed trait TrafficLight
  case class Red() extends TrafficLight
  case class Amber() extends TrafficLight
  case class Green() extends TrafficLight

  they should "support sealed trait hierarchies" in {
    val pf = PartialCodec[TrafficLight]

    val lights = Table(
      "light",
      Red(),
      Amber(),
      Green())

    forAll(lights) { light =>
      val buffer = new Array[Byte](pf.size(light))

      pf.encode(light, 0, buffer)

      val result = pf.decode(0, buffer).right.value

      result.decoded shouldBe light
      result.nextIndex shouldBe buffer.length
    }
  }

  they should "support option" in {
    val pf = PartialCodec[Option[String]]

    val choices = Table(
      "choice",
      Some("thing"),
      None)

    forAll(choices) { choice =>
      val buffer = new Array[Byte](pf.size(choice))

      pf.encode(choice, 0, buffer)

      val result = pf.decode(0, buffer).right.value

      result.decoded shouldBe choice
      result.nextIndex shouldBe buffer.length
    }
  }

  they should "support Either" in {
    val pf = PartialCodec[Either[String, Int]]

    val choices = Table(
      "choice",
      Left("thing"),
      Right(10))

    forAll(choices) { choice =>
      val buffer = new Array[Byte](pf.size(choice))

      pf.encode(choice, 0, buffer)

      val result = pf.decode(0, buffer).right.value

      result.decoded shouldBe choice
      result.nextIndex shouldBe buffer.length
    }
  }
}
