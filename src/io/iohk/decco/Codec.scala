package io.iohk.decco

import io.iohk.decco.PartialCodec.{DecodeResult, Failure}

final class Codec[T](val partial: PartialCodec[T]) extends Ordered[Codec[T]] {

  import Codec._

  def encode(t: T): Array[Byte] = {
    val headerSize = headerPf.size((0, partial.typeCode))
    val size: Int = headerSize + partial.size(t)
    val r = new Array[Byte](size)
    headerPf.encode((size, partial.typeCode), 0, r)
    partial.encode(t, headerSize, r)
    r
  }

  def decode(source: Array[Byte]): Option[T] = {
    headerPf.decode(0, source) match {
      case Left(Failure) =>
        None
      case Right(DecodeResult((_, typeField), nextIndex)) =>
        if (typeField == partial.typeCode)
          partial.decode(nextIndex, source) match {
            case Right(DecodeResult(t, _)) =>
              Some(t)
            case Left(Failure) =>
              None
          }
        else
          None
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

  import NativeCodecInstances.{IntPartialCodec, StringPartialCodec}

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
}
