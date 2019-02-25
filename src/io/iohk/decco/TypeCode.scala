package io.iohk.decco

import shapeless.{:+:, ::, Coproduct, HList}

import scala.language.higherKinds
import scala.reflect.runtime.universe._

case class TypeCode[T](id: String) {
  override def toString: String = id
}

object TypeCode {

  def apply[T](implicit t: TypeTag[T]): TypeCode[T] = {
    TypeCode(t.tpe.toString)
  }

  def genTypeCode[C[_], V](implicit wtt: WeakTypeTag[C[_]], pcv: PartialCodec[V]): TypeCode[C[V]] = {
    TypeCode[C[V]](wtt.tpe.toString.replace("[_]", s"[${pcv.typeCode.id}]"))
  }

  def genTypeCode[C[_, _], K, V](
      implicit wtt: WeakTypeTag[C[_, _]],
      pck: PartialCodec[K],
      pcv: PartialCodec[V]
  ): TypeCode[C[K, V]] = {
    TypeCode(wtt.tpe.toString.replace("[_, _]", s"[${pck.typeCode.id},${pcv.typeCode.id}]"))
  }

  def tuple2TypeCode[U, V](u: TypeCode[U], v: TypeCode[V]): TypeCode[(U, V)] = {
    TypeCode(s"(${u.id}, ${v.id})")
  }

  def hConsTypeCode[H, T <: HList](h: TypeCode[H], t: TypeCode[T]): TypeCode[H :: T] = {
    TypeCode(s"${h.id} :: ${t.id}")
  }

  def cConsTypeCode[H, T <: Coproduct](h: TypeCode[H], t: TypeCode[T]): TypeCode[H :+: T] = {
    TypeCode(s"${h.id} :+: ${t.id}")
  }
}
