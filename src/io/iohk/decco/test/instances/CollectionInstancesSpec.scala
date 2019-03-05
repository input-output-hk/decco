package io.iohk.decco.instances

import java.nio.ByteBuffer

import io.iohk.decco.PartialCodec
import io.iohk.decco.TestingHelpers.partialCodecTest
import io.iohk.decco.auto._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FlatSpec

import scala.collection.immutable.{HashSet, ListSet, NumericRange, Queue, TreeSet}
import scala.collection.{LinearSeq, SortedMap, SortedSet}
import scala.util.Random

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
    partialCodecTest[A]
    partialCodecTest[Map[A, A]]
  }

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

  they should "encode and decode native array types" in {
    partialCodecTest[Array[Byte]]
    partialCodecTest[Array[Short]]
    partialCodecTest[Array[Char]]
    partialCodecTest[Array[Int]]
    partialCodecTest[Array[Long]]
    partialCodecTest[Array[Float]]
    partialCodecTest[Array[Double]]
    partialCodecTest[Array[Boolean]]
  }

  they should "not explode the stack for large collections" in {
    import org.scalatest.EitherValues._
    import org.scalatest.Matchers._
    val codec = PartialCodec[Array[Byte]]
    val data = randomBytes(10*1024*1024)
    val buff = ByteBuffer.allocate(10*1024*1024 + 4)
    codec.encode(data, 0, buff)
    val result = codec.decode(0, buff).right.value
    result.decoded shouldBe data
    result.nextIndex shouldBe buff.capacity
  }

  private def randomBytes(n: Int): Array[Byte] = {
    val a = new Array[Byte](n)
    Random.nextBytes(a)
    a
  }

}
