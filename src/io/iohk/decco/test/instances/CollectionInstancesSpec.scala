package io.iohk.decco
package test.instances

import io.iohk.decco.test.utils.CodecTestingHelpers._
import io.iohk.decco.auto._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FlatSpec

import scala.collection.immutable.{HashSet, ListSet, NumericRange, Queue, TreeSet}
import scala.collection.{LinearSeq, SortedMap, SortedSet}

class CollectionInstancesSpec extends FlatSpec {

  behavior of "CollectionInstances"

  case class A(s: String)

  implicit val arbitraryA: Arbitrary[A] = Arbitrary(arbitrary[String].map(A))

  implicit val arbitraryRange: Arbitrary[Range] = Arbitrary(
    for {
      start <- Gen.choose(Int.MinValue, Int.MaxValue)
      end <- Gen.choose(start, Int.MaxValue)

    } yield Range(start, end, (end - start) / 10)
  )

  implicit def arbitraryNumericRange: Arbitrary[NumericRange[Long]] = Arbitrary(
    for {
      start <- Gen.choose(Long.MinValue, Long.MaxValue)
      end <- Gen.choose(start, Long.MaxValue)

    } yield NumericRange(start, end, (end - start) / 10)
  )

  they should "work for this case" in {
    testCodec[A]
    testCodec[Map[A, A]]
  }

  they should "encode and decode collection types" in {
    // interfaces in the collection hierarchy
    testCodec[Traversable[String]]
    testCodec[Iterable[String]]
    testCodec[Set[String]]
    testCodec[SortedSet[String]]
    testCodec[Map[String, String]]
    testCodec[SortedMap[String, String]]
    testCodec[Seq[String]]
    testCodec[IndexedSeq[String]]
    testCodec[LinearSeq[String]]

    // concrete types in the collection hierarchy
    testCodec[HashSet[String]]
    testCodec[ListSet[String]]
    testCodec[TreeSet[String]]

    testCodec[Vector[String]]
    testCodec[String]
    testCodec[List[String]]
    testCodec[Stream[String]]
    testCodec[Queue[String]]

    testCodec[Range]
    testCodec[NumericRange[Long]]

    testCodec[Array[String]]
  }
}
