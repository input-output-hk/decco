package io.iohk.decco

import java.nio.ByteBuffer

import io.iohk.decco.PartialCodec.{DecodeResult, Failure}

abstract class Codec[T](val partial: PartialCodec[T]) extends Ordered[Codec[T]] {

  import Codec._

  def newBuffer(capacity: Int): ByteBuffer

  def encode(t: T): ByteBuffer = {
    val bodySize = partial.size(t)
    val header = (bodySize, partial.typeCode)
    val headerSize = headerPf.size(header)
    val r = ByteBuffer.allocate(bodySize + headerSize)
    headerPf.encode(header, 0, r)
    partial.encode(t, headerSize, r)
    r
  }

  def decode(source: ByteBuffer): Either[DecodeFailure, T] = {
    headerPf.decode(0, source) match {
      case Left(Failure) =>
        Left(HeaderWrongFormat)
      case Right(DecodeResult((sizeField, typeField), nextIndex)) =>
        if (typeField == partial.typeCode)
          if (sizeField <= source.remaining - nextIndex) {
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

  /*
   * From ByteBuffer docs:
   * A direct byte buffer may be created by invoking the allocateDirect factory method of this class.
   * The buffers returned by this method typically have somewhat higher allocation and deallocation costs than non-direct buffers.
   * The contents of direct buffers may reside outside of the normal garbage-collected heap,
   * and so their impact upon the memory footprint of an application might not be obvious.
   * It is therefore recommended that direct buffers be allocated primarily for large, long-lived buffers
   * that are subject to the underlying system's native I/O operations.
   * In general it is best to allocate direct buffers only when they yield a measureable gain in program performance.
   */
  def heapCodec[T](implicit ev: PartialCodec[T]): Codec[T] = new Codec[T](ev) {
    override def newBuffer(capacity: Int): ByteBuffer = ByteBuffer.allocate(capacity)
  }

  def directCodec[T](implicit ev: PartialCodec[T]): Codec[T] = new Codec[T](ev) {
    override def newBuffer(capacity: Int): ByteBuffer = ByteBuffer.allocateDirect(capacity)
  }

  import io.iohk.decco.auto._

  val headerPf = PartialCodec[(Int, String)]

  // 'Rehydrating' function.
  // From a buffer of unknown type, read the typeCode and apply the
  // data to a function that maps to a decoder with the corresponding typeCode.
  def decodeFrame(decoderWrappers: Map[String, (Int, ByteBuffer) => Unit], start: Int, source: ByteBuffer): Unit = {
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
