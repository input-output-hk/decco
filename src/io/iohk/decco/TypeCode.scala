package io.iohk.decco

import shapeless.{:+:, ::, Coproduct, HList}

import scala.collection.immutable.{HashMap, HashSet, ListMap, ListSet, NumericRange, Queue, TreeMap, TreeSet}
import scala.collection.{LinearSeq, SortedMap, SortedSet}
import scala.language.higherKinds
import scala.reflect.runtime.universe._

case class TypeCode[T](id: String) {
  override def toString: String = id
}

object TypeCode {

  val Traversable = TypeCode[Traversable[_]]("scala.collection.Traversable[_]")
  val Iterable = TypeCode[Iterable[_]]("scala.collection.Iterable[_]")
  val List = TypeCode[List[_]]("scala.collection.List[_]")
  val Set = TypeCode[Set[_]]("scala.collection.Set[_]")
  val SortedSet = TypeCode[SortedSet[_]]("scala.collection.SortedSet[_]")
  val Map = TypeCode[Map[_, _]]("scala.collection.Map[_, _]")
  val SortedMap = TypeCode[SortedMap[_, _]]("scala.collection.SortedMap[_, _]")
  val Seq = TypeCode[Seq[_]]("scala.collection.Seq[_]")
  val IndexedSeq = TypeCode[IndexedSeq[_]]("scala.collection.IndexedSeq[_]")
  val LinearSeq = TypeCode[LinearSeq[_]]("scala.collection.LinearSeq[_]")
  val HashSet = TypeCode[HashSet[_]]("scala.collection.immutable.HashSet[_]")
  val ListSet = TypeCode[ListSet[_]]("scala.collection.immutable.ListSet[_]")
  val TreeSet = TypeCode[TreeSet[_]]("scala.collection.immutable.TreeSet[_]")
  val Vector = TypeCode[Vector[_]]("scala.collection.immutable.Vector[_]")
  val NumericRange = TypeCode[NumericRange[_]]("scala.collection.immutable.NumericRange[_]")
  val Stream = TypeCode[Stream[_]]("scala.collection.immutable.Stream[_]")
  val Queue = TypeCode[Queue[_]]("scala.collection.immutable.Queue[_]")
  val HashMap = TypeCode[HashMap[_, _]]("scala.collection.immutable.HashMap[_, _]")
  val ListMap = TypeCode[ListMap[_, _]]("scala.collection.immutable.ListMap[_, _]")
  val TreeMap = TypeCode[TreeMap[_, _]]("scala.collection.immutable.TreeMap[_, _]")
  val Array = TypeCode[Array[_]]("scala.Array[_]")

  implicit def apply[T](implicit t: TypeTag[T]): TypeCode[T] = {
    TypeCode(t.tpe.toString)
  }

  def apply[T[_], V](implicit t: TypeTag[T[_]], v: TypeTag[V]): TypeCode[T[V]] = {
    val ts = t.tpe.toString
    val vs = v.tpe.toString
    val tt = ts.replace("[_]", s"[$vs]")
    TypeCode(tt)
  }

  def apply[T[_, _], U, V](implicit t: TypeTag[T[_, _]], u: TypeTag[U], v: TypeTag[V]): TypeCode[T[U, V]] = {
    val ts = t.tpe.toString
    val us = u.tpe.toString
    val vs = v.tpe.toString
    val tt = ts.replace("[_, _]", s"[$us,$vs]")
    TypeCode(tt)
  }

  def collectionTypeCode[C[_], V](c: TypeCode[C[_]], v: TypeCode[V]): TypeCode[C[V]] = {
    TypeCode(c.id.replace("[_]", s"[${v.id}]"))
  }

  def collectionTypeCode[C[_, _], K, V](c: TypeCode[C[_, _]], k: TypeCode[K], v: TypeCode[V]): TypeCode[C[K, V]] = {
    TypeCode(c.id.replace("[_, _]", s"[${k.id}, ${v.id}]"))
  }

  //  def collectionTypeCode(c: TypeCode, t: TypeCode): TypeCode = {
//    TypeCode(c.id.replace("[_]", s"[${t.id}]"))
//  }
//
//  def collectionTypeCode(c: TypeCode, k: TypeCode, v: TypeCode): TypeCode = {
//    TypeCode(c.id.replace("[_, _]", s"[${k.id}, ${v.id}]"))
//  }

//  def tuple2TypeCode(t: TypeCode, u: TypeCode): TypeCode = {
//    TypeCode(s"(${t.id}, ${u.id}")
//  }

  def tuple2TypeCode[U, V](u: TypeCode[U], v: TypeCode[V]): TypeCode[(U, V)] = {
    val us = u.id
    val vs = v.id
    TypeCode(s"($us, $vs)")
  }

  def hConsTypeCode[H, T <: HList](h: TypeCode[H], t: TypeCode[T]): TypeCode[H :: T] =
    TypeCode(s"${h.id} :: ${t.id}")


  def cConsTypeCode[H, T <: Coproduct](h: TypeCode[H], t: TypeCode[T]): TypeCode[H :+: T] =
    TypeCode(s"${h.id} :+: ${t.id}")
//  def cConsTypeCode(h: TypeCode, t: TypeCode): TypeCode = {
//    TypeCode(s"${h.id} shapeless.:+: ${t.id}")
//  }
}