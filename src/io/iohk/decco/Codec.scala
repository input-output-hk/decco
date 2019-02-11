package io.iohk.decco

import io.iohk.decco.PartialCodec.{DecodeResult, Failure}

final class Codec[T](val partial: PartialCodec[T]) extends Ordered[Codec[T]] {

  import Codec._

  def encode(t: T): Array[Byte] = {
    val bodySize = partial.size(t)
    val header = (bodySize, partial.typeCode)
    val headerSize = headerPf.size(header)
    val r = new Array[Byte](bodySize + headerSize)
    headerPf.encode(header, 0, r)
    partial.encode(t, headerSize, r)
    r
  }

  def decode(source: Array[Byte]): Either[DecodeFailure, T] = {
    headerPf.decode(0, source) match {
      case Left(Failure) =>
        Left(HeaderWrongFormat)
      case Right(DecodeResult((sizeField, typeField), nextIndex)) =>
        if (typeField == partial.typeCode)
          if (sizeField <= source.length - nextIndex) {
            partial.decode(nextIndex, source) match {
              case Right(DecodeResult(t, _)) =>
                Right(t)
              case Left(Failure) =>
                Left(BodyWrongFormat)
            }
          } else {
            Left(BodyTooShort)
          } else
          Left(BodyWrongType)
    }
  }

  override def hashCode(): Int = this.partial.typeCode.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case that: Codec[T] => this.partial.typeCode == that.partial.typeCode
    case _ => false
  }

  override def compare(that: Codec[T]): Int =
    this.partial.typeCode.compare(that.partial.typeCode)

}

object Codec {

  import io.iohk.decco.instances.NativeInstances.{IntPartialCodec, StringPartialCodec}

  val headerPf: PartialCodec[(Int, String)] = IntPartialCodec.zip(StringPartialCodec)

  def apply[T](implicit ev: PartialCodec[T]): Codec[T] = codecFromPartialCodec(ev)

  implicit def codecFromPartialCodec[T](implicit partial: PartialCodec[T]): Codec[T] =
    new Codec(partial)

  // 'Rehydrating' function.
  // From a buffer of unknown type, read the typeCode and apply the
  // data to a function that maps to a decoder with the corresponding typeCode.
  def decodeFrame(decoderWrappers: Map[String, (Int, Array[Byte]) => Unit], start: Int, source: Array[Byte]): Unit = {
    headerPf.decode(start, source) match {
      case Right(DecodeResult((_, typeField), nextIndex)) =>
        decoderWrappers.get(typeField).foreach(decoderWrapper => decoderWrapper(nextIndex, source))
      case _ =>
        ()
    }
  }

  sealed trait DecodeFailure

  case object HeaderWrongFormat extends DecodeFailure

  case object BodyTooShort extends DecodeFailure

  case object BodyWrongType extends DecodeFailure

  case object BodyWrongFormat extends DecodeFailure

}
