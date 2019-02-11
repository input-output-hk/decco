package io.iohk.decco.instances

import io.iohk.decco.PartialCodec
import io.iohk.decco.auto._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.EitherValues._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.GeneratorDrivenPropertyChecks._

class ArrayInstancesSpec extends FlatSpec {

  behavior of "ArrayInstances"

  case class A(s: String)
  implicit val arbitraryA: Arbitrary[A] = Arbitrary(arbitrary[String].map(A))

  they should "encode and decode variable width types properly" in {
    forAll { as: Array[A] =>
      val codec = PartialCodec[Array[A]]
      val buffer = new Array[Byte](codec.size(as))
      codec.encode(as, 0, buffer)

      codec.decode(0, buffer).right.value.decoded shouldBe as
    }
  }
}
