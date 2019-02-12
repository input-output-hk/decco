package io.iohk.decco.instances

import io.iohk.decco.PartialCodec
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

class ArrayCodec[T: ClassTag: TypeTag](implicit iCodec: PartialCodec[Int], tCodec: PartialCodec[T])
    extends TraversableCodec[T, Array[T]]
