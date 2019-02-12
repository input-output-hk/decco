package io.iohk.decco.instances

import io.iohk.decco.PartialCodec
import io.iohk.decco.TestingHelpers.encodeDecodeTest
import io.iohk.decco.auto._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.FlatSpec
import org.scalatest.EitherValues._
import org.scalatest.Matchers._

import scala.collection.immutable.{HashSet, ListSet, NumericRange, Queue, TreeSet}
import scala.collection.{LinearSeq, SortedMap, SortedSet}

class CollectionInstancesSpec extends FlatSpec {

  behavior of "ArrayInstances"

  case class A(s: String)

  implicit val arbitraryA: Arbitrary[A] = Arbitrary(arbitrary[String].map(A))

  they should "encode and decode collection types" in {

    // interfaces in the collection hierarchy
    encodeDecodeTest[Traversable[String]]
    encodeDecodeTest[Iterable[String]]
    encodeDecodeTest[Set[String]]
    encodeDecodeTest[SortedSet[String]]
    encodeDecodeTest[Map[String, String]]
    encodeDecodeTest[SortedMap[String, String]]
    encodeDecodeTest[Seq[String]]
    encodeDecodeTest[IndexedSeq[String]]
    encodeDecodeTest[LinearSeq[String]]

    // concrete types in the collection hierarchy
    encodeDecodeTest[HashSet[String]]
    encodeDecodeTest[ListSet[String]]
    encodeDecodeTest[TreeSet[String]]

    encodeDecodeTest[Vector[String]]
    encodeDecodeTest[String]
    encodeDecodeTest[List[String]]
    encodeDecodeTest[Stream[String]]
    encodeDecodeTest[Queue[String]]

    encodeDecodeTest[Array[String]]

  }

  import org.scalatest.prop.TableDrivenPropertyChecks._

  they should "encode and decode Ranges" in {
    val ranges = Table(
      "range",
      Range(10, 100, 5),
      Range.inclusive(10, 100, 5)
    )

    forAll(ranges) { range =>
      val codec = PartialCodec[Range]

      val arr = new Array[Byte](codec.size(range))

      codec.encode(range, 0, arr)

      val result = codec.decode(0, arr).right.value
      result.decoded shouldBe range
      result.nextIndex shouldBe arr.length
    }
  }

  they should "encode and decode NumericRanges" in {

    val ranges = Table(
      "range",
      NumericRange[Long](10, 100, 5),
      NumericRange.inclusive[Long](10, 100, 5)
    )

    forAll(ranges) { range =>
      val codec = PartialCodec[NumericRange[Long]]

      val arr = new Array[Byte](codec.size(range))

      codec.encode(range, 0, arr)

      val result = codec.decode(0, arr).right.value
      result.decoded shouldBe range
      result.nextIndex shouldBe arr.length
    }
  }
}
