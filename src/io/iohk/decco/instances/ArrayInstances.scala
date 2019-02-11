package io.iohk.decco.instances

import io.iohk.decco.PartialCodec

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

trait ArrayInstances {

  implicit def ArrayInstance[T: ClassTag: TypeTag](
      implicit tCodec: PartialCodec[T],
      iCodec: PartialCodec[Int]
  ): PartialCodec[Array[T]] = {
    new ArrayCodec[T]
  }
}

object ArrayInstances extends ArrayInstances
