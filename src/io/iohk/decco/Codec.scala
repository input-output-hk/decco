package io.iohk.decco

import java.nio.ByteBuffer

import io.iohk.decco.PartialCodec.{DecodeResult, Failure}

abstract class Codec[T](private val partialCodec: PartialCodec[T]) extends Ordered[Codec[T]] {

  import Codec._
  import DecodeFailure._

  def newBuffer(capacity: Int): ByteBuffer

  def encode(t: T): ByteBuffer = {
    val bodySize = partialCodec.size(t)
    val header = (bodySize, partialCodec.typeCode)
    val headerSize = headerCodec.size(header)
    val r = ByteBuffer.allocate(bodySize + headerSize)
    headerCodec.encode(header, 0, r)
    partialCodec.encode(t, headerSize, r)
    r
  }

  def decode(source: ByteBuffer): Either[DecodeFailure, T] = {
    headerCodec.decode(0, source) match {
      case Left(Failure) =>
        Left(HeaderWrongFormat)
      case Right(DecodeResult((sizeField, typeField), nextIndex)) =>
        decodeBody(source, sizeField, typeField, nextIndex)
    }
  }

  override def hashCode(): Int = this.partialCodec.typeCode.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case that: Codec[T] => this.partialCodec.typeCode == that.partialCodec.typeCode
    case _ => false
  }

  override def compare(that: Codec[T]): Int =
    this.partialCodec.typeCode.compare(that.partialCodec.typeCode)

  private def decodeBody(source: ByteBuffer, sizeField: Int, typeField: String, nextIndex: Int): Either[DecodeFailure, T] = {
    if (typeField == partialCodec.typeCode) {
      if (sizeField <= source.remaining - nextIndex) {
        partialCodec.decode(nextIndex, source) match {
          case Right(DecodeResult(t, _)) =>
            Right(t)
          case Left(Failure) =>
            Left(BodyWrongFormat)
        }
      } else {
        Left(BodyTooShort)
      }
    } else {
      Left(BodyWrongType)
    }
  }
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

  private[decco] val headerCodec = PartialCodec[(Int, String)]

  // 'Rehydrating' function.
  // From a buffer of unknown type, read the typeCode and apply the
  // data to a function that maps to a decoder with the corresponding typeCode.
  def decodeFrame(decoderWrappers: Map[String, (Int, ByteBuffer) => Unit], start: Int, source: ByteBuffer): Unit = {
    headerCodec.decode(start, source) match {
      case Right(DecodeResult((_, typeField), nextIndex)) =>
        decoderWrappers.get(typeField).foreach(decoderWrapper => decoderWrapper(nextIndex, source))
      case _ =>
        ()
    }
  }
}
