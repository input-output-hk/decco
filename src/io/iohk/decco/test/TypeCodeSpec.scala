package io.iohk.decco

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{:+:, ::, CNil, HNil}

import scala.collection.{LinearSeq, SortedMap, SortedSet}
import scala.collection.immutable.{HashMap, HashSet, ListMap, ListSet, NumericRange, Queue, TreeMap, TreeSet}

class TypeCodeSpec extends FlatSpec {

  behavior of "type code"

  case class A()
  case class B()
  case class Wrap[A, B]()

  it should "infer type codes for collections" in {
    TypeCode[Traversable, A] shouldBe TypeCode[Traversable[A]]
    TypeCode[Iterable, A] shouldBe TypeCode[Iterable[A]]
    TypeCode[List, A] shouldBe TypeCode[List[A]]
    TypeCode[Set, A] shouldBe TypeCode[Set[A]]
    TypeCode[SortedSet, A] shouldBe TypeCode[SortedSet[A]]
    TypeCode[Map, A, B] shouldBe TypeCode[Map[A, B]]
    TypeCode[SortedMap, A, B] shouldBe TypeCode[SortedMap[A, B]]
    TypeCode[Seq, A] shouldBe TypeCode[Seq, A]
    TypeCode[IndexedSeq, A] shouldBe TypeCode[IndexedSeq, A]
    TypeCode[LinearSeq, A] shouldBe TypeCode[LinearSeq, A]
    TypeCode[HashSet, A] shouldBe TypeCode[HashSet, A]
    TypeCode[ListSet, A] shouldBe TypeCode[ListSet, A]
    TypeCode[TreeSet, A] shouldBe TypeCode[TreeSet, A]
    TypeCode[Vector, A] shouldBe TypeCode[Vector, A]
    TypeCode[NumericRange, A] shouldBe TypeCode[NumericRange, A]
    TypeCode[Stream, A] shouldBe TypeCode[Stream, A]
    TypeCode[Queue, A] shouldBe TypeCode[Queue, A]
    TypeCode[HashMap, A, B] shouldBe TypeCode[HashMap[A, B]]
    TypeCode[ListMap, A, B] shouldBe TypeCode[ListMap[A, B]]
    TypeCode[TreeMap, A, B] shouldBe TypeCode[TreeMap[A, B]]
    TypeCode[Array, A]  shouldBe TypeCode[Array, A]
  }

  it should "infer type codes for tuples" in {
    TypeCode.tuple2TypeCode[A, B](TypeCode[A], TypeCode[B]) shouldBe TypeCode[(A, B)]
  }

  it should "infer type codes for wrapper types" in {
    TypeCode[Wrap, A, B] shouldBe TypeCode[Wrap[A, B]]
  }

  it should "infer type codes for HLists" in {
    TypeCode.hConsTypeCode(TypeCode[A], TypeCode[HNil]) shouldBe TypeCode[A :: HNil]
  }

  it should "infer type codes for Coproducts" in {
    TypeCode.cConsTypeCode(TypeCode[A], TypeCode[CNil]) shouldBe TypeCode[A :+: CNil]
  }
}
