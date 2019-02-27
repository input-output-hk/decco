package io.iohk.decco

import scala.language.higherKinds
import scala.reflect.runtime.universe._

case class TypeCode[T](id: String) {
  override def toString: String = id
}

object TypeCode extends SecondaryTypeCodes {

  implicit def apply[T](implicit t: TypeTag[T]): TypeCode[T] = {
    TypeCode(t.tpe.toString)
  }
}

trait SecondaryTypeCodes {
  implicit def genTypeCode[C[_], V](implicit wtt: WeakTypeTag[C[_]], cv: Codec[V]): TypeCode[C[V]] = {
    TypeCode[C[V]](wtt.tpe.toString.replace("[_]", s"[${cv.typeCode.id}]"))
  }

  implicit def genTypeCode[C[_, _], K, V](
      implicit wtt: WeakTypeTag[C[_, _]],
      ck: Codec[K],
      cv: Codec[V]
  ): TypeCode[C[K, V]] = {
    TypeCode(wtt.tpe.toString.replace("[_, _]", s"[${ck.typeCode.id},${cv.typeCode.id}]"))
  }
}
