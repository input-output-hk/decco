package io.iohk.decco

import java.nio.ByteBuffer

import io.iohk.decco.PartialCodec.{DecodeResult, Failure}

abstract class Codec[T](val partialCodec: PartialCodec[T], val typeCode: TypeCode[T]) extends Ordered[Codec[T]] {

  import Codec._
  import DecodeFailure._

  def newBuffer(capacity: Int): ByteBuffer

  def encode(t: T): ByteBuffer = {
    val bodySize = partialCodec.size(t)
    val header = (bodySize, MD5(typeCode.id))
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

  override def hashCode(): Int = this.typeCode.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case that: Codec[T] => this.typeCode == that.typeCode
    case _ => false
  }

  override def compare(that: Codec[T]): Int =
    this.typeCode.id.compare(that.typeCode.id)

  private def decodeBody(
      source: ByteBuffer,
      sizeField: Int,
      typeField: MD5,
      nextIndex: Int
  ): Either[DecodeFailure, T] = {
    if (typeField == MD5(typeCode.id)) {
      if (sizeField <= source.remaining - nextIndex) {
        partialCodec.decode(nextIndex, source) match {
          case Right(DecodeResult(t, _)) =>
            Right(t)
          case Left(Failure) =>
            Left(BodyWrongFormat)
        }
      } else {
        Left(BodyTooShort(source.remaining - nextIndex, sizeField))
      }
    } else {
      Left(BodyWrongType(MD5(typeCode.id), typeField))
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
  def heapCodec[T](implicit pc: PartialCodec[T], typeCode: TypeCode[T]): Codec[T] = new Codec[T](pc, typeCode) {
    override def newBuffer(capacity: Int): ByteBuffer = ByteBuffer.allocate(capacity)
  }

  def directCodec[T](implicit pc: PartialCodec[T], typeCode: TypeCode[T]): Codec[T] = new Codec[T](pc, typeCode) {
    override def newBuffer(capacity: Int): ByteBuffer = ByteBuffer.allocateDirect(capacity)
  }

  implicit def apply[T](implicit pc: PartialCodec[T], typeCode: TypeCode[T]): Codec[T] = heapCodec[T](pc, typeCode)

  import io.iohk.decco.auto._

  private[decco] val headerCodec: PartialCodec[(Int, MD5)] = IntPartialCodec.zip(MD5.md5Codec)

  // 'Rehydrating' function.
  // From a buffer of unknown type, read the typeCode and apply the
  // data to a function that maps to a decoder with the corresponding typeCode.
  def decodeFrame(decoderWrappers: Map[String, (Int, ByteBuffer) => Unit], start: Int, source: ByteBuffer): Unit = {
    val hashWrappers: Map[MD5, (Int, ByteBuffer) => Unit] = decoderWrappers.map {
      case (typeCode, decoderWrapper) => (MD5(typeCode), decoderWrapper)
    }

    headerCodec.decode(start, source) match {
      case Right(DecodeResult((_, typeField), nextIndex)) =>
        hashWrappers.get(typeField).foreach(decoderWrapper => decoderWrapper(nextIndex, source))
      case _ =>
        ()
    }
  }
}
