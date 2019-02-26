package io.iohk.decco

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import scala.collection.{LinearSeq, SortedMap, SortedSet}
import scala.collection.immutable.{HashMap, HashSet, ListMap, ListSet, NumericRange, Queue, TreeMap, TreeSet}
import auto._
import io.iohk.decco.TypeCode.genTypeCode

class TypeCodeSpec extends FlatSpec {

  behavior of "type code"

  case class A()
  case class B()
  case class Wrap1[A]()
  case class Wrap2[A, B]()

  implicit val pca = PartialCodec[A]
  implicit val pcb = PartialCodec[B]

  it should "infer type codes for collections" in {
    genTypeCode[Traversable, A] shouldBe TypeCode[Traversable[A]]
    genTypeCode[Iterable, A] shouldBe TypeCode[Iterable[A]]
    genTypeCode[List, A] shouldBe TypeCode[List[A]]
    genTypeCode[Set, A] shouldBe TypeCode[Set[A]]
    genTypeCode[SortedSet, A] shouldBe TypeCode[SortedSet[A]]
    genTypeCode[Map, A, B] shouldBe TypeCode[Map[A, B]]
    genTypeCode[SortedMap, A, B] shouldBe TypeCode[SortedMap[A, B]]
    genTypeCode[Seq, A] shouldBe TypeCode[Seq[A]]
    genTypeCode[IndexedSeq, A] shouldBe TypeCode[IndexedSeq[A]]
    genTypeCode[LinearSeq, A] shouldBe TypeCode[LinearSeq[A]]
    genTypeCode[HashSet, A] shouldBe TypeCode[HashSet[A]]
    genTypeCode[ListSet, A] shouldBe TypeCode[ListSet[A]]
    genTypeCode[TreeSet, A] shouldBe TypeCode[TreeSet[A]]
    genTypeCode[Vector, A] shouldBe TypeCode[Vector[A]]
    genTypeCode[NumericRange, A] shouldBe TypeCode[NumericRange[A]]
    genTypeCode[Stream, A] shouldBe TypeCode[Stream[A]]
    genTypeCode[Queue, A] shouldBe TypeCode[Queue[A]]
    genTypeCode[HashMap, A, B] shouldBe TypeCode[HashMap[A, B]]
    genTypeCode[ListMap, A, B] shouldBe TypeCode[ListMap[A, B]]
    genTypeCode[TreeMap, A, B] shouldBe TypeCode[TreeMap[A, B]]
    genTypeCode[Array, A] shouldBe TypeCode[Array[A]]
  }

  it should "infer type codes for single wrapper types" in {
    genTypeCode[Wrap1, A] shouldBe TypeCode[Wrap1[A]]
  }

  it should "infer type codes for dual wrapper types" in {
    genTypeCode[Wrap2, A, B] shouldBe TypeCode[Wrap2[A, B]]
  }
}
