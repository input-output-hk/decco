package io.iohk.decco.instances

import io.iohk.decco.TestingHelpers.partialCodecTest
import io.iohk.decco.auto._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FlatSpec

import scala.collection.immutable.{HashSet, ListSet, NumericRange, Queue, TreeSet}
import scala.collection.{LinearSeq, SortedMap, SortedSet}

class CollectionInstancesSpec extends FlatSpec {

  behavior of "ArrayInstances"

  case class A(s: String)

  implicit val arbitraryA: Arbitrary[A] = Arbitrary(arbitrary[String].map(A))

  implicit val arbitraryRange: Arbitrary[Range] = Arbitrary(
    for {
      start <- Gen.choose(0, Int.MaxValue)
      end <- Gen.choose(start, Int.MaxValue)

    } yield Range(start, end, (end - start)/10)
  )

  implicit def arbitraryNumericRange: Arbitrary[NumericRange[Long]] = Arbitrary(
    for {
      start <- Gen.choose(0, Long.MaxValue)
      end <- Gen.choose(start, Long.MaxValue)

    } yield NumericRange(start, end, (end - start)/10)
  )

  they should "encode and decode collection types" in {

    // interfaces in the collection hierarchy
    partialCodecTest[Traversable[String]]
    partialCodecTest[Iterable[String]]
    partialCodecTest[Set[String]]
    partialCodecTest[SortedSet[String]]
    partialCodecTest[Map[String, String]]
    partialCodecTest[SortedMap[String, String]]
    partialCodecTest[Seq[String]]
    partialCodecTest[IndexedSeq[String]]
    partialCodecTest[LinearSeq[String]]

    // concrete types in the collection hierarchy
    partialCodecTest[HashSet[String]]
    partialCodecTest[ListSet[String]]
    partialCodecTest[TreeSet[String]]

    partialCodecTest[Vector[String]]
    partialCodecTest[String]
    partialCodecTest[List[String]]
    partialCodecTest[Stream[String]]
    partialCodecTest[Queue[String]]

    partialCodecTest[Range]
    partialCodecTest[NumericRange[Long]]

    partialCodecTest[Array[String]]
  }
}
