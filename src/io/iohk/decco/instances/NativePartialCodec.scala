package io.iohk.decco.instances

import io.iohk.decco.PartialCodec
import io.iohk.decco.PartialCodec.{DecodeResult, Failure, typeTagCode}
import scala.reflect.runtime.universe.TypeTag

abstract class NativePartialCodec[T: TypeTag](val size: Int) extends PartialCodec[T] {
  override def size(t: T): Int = size

  override def typeCode: String = typeTagCode[T]

  def doDecode(start: Int, source: Array[Byte]): T

  override def decode(start: Int, source: Array[Byte]): Either[Failure, DecodeResult[T]] = {
    if (start < 0 || start >= source.length || start + size > source.length)
      Left(Failure)
    else
      Right(DecodeResult(doDecode(start, source), start + size))
  }
}
