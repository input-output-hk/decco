package io.iohk.decco.instances

import java.nio.ByteBuffer

import io.iohk.decco.PartialCodec
import io.iohk.decco.PartialCodec.{DecodeResult, Failure, typeTagCode}

import scala.reflect.runtime.universe.TypeTag

abstract class NativePartialCodec[T: TypeTag](val size: Int) extends PartialCodec[T] {
  override def size(t: T): Int = size

  override def typeCode: String = typeTagCode[T]

  def doDecode(start: Int, source: ByteBuffer): T

  override def decode(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[T]] = {
    if (start < 0 || start >= source.remaining || start + size > source.remaining)
      Left(Failure)
    else
      Right(DecodeResult(doDecode(start, source), start + size))
  }
}
