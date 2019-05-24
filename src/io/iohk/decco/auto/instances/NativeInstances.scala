package io.iohk.decco
package auto.instances

import java.nio._
import Codec._
import scala.reflect.ClassTag

trait NativeInstances {

  private def bool2byte(b: Boolean): Byte = if (b) 1 else 0
  private def byte2bool(b: Byte): Boolean = b != 0

  private def toArray(byteBuffer: ByteBuffer): Array[Byte] = {
    if (byteBuffer.hasArray)
      byteBuffer.array
    else {
      (byteBuffer: java.nio.Buffer).position(0)
      val arr = new Array[Byte](byteBuffer.remaining())
      byteBuffer.get(arr)
      arr
    }
  }

  private def toByteBuffer(arr: Array[Byte]): ByteBuffer = ByteBuffer.wrap(arr)

  implicit val ByteCodec: Codec[Byte] = new NativeCodec[Byte](size = 1, _.put(_, _), _.get(_))
  implicit val ShortCodec: Codec[Short] = new NativeCodec[Short](size = 2, _.putShort(_, _), _.getShort(_))
  implicit val IntCodec: Codec[Int] = new NativeCodec[Int](size = 4, _.putInt(_, _), _.getInt(_))
  implicit val LongCodec: Codec[Long] = new NativeCodec[Long](size = 8, _.putLong(_, _), _.getLong(_))
  implicit val CharCodec: Codec[Char] = new NativeCodec[Char](size = 2, _.putChar(_, _), _.getChar(_))
  implicit val FloatCodec: Codec[Float] = new NativeCodec[Float](size = 4, _.putFloat(_, _), _.getFloat(_))
  implicit val DoubleCodec: Codec[Double] = new NativeCodec[Double](8, _.putDouble(_, _), _.getDouble(_))

  implicit val BooleanCodec: Codec[Boolean] = ByteCodec.map(byte2bool, bool2byte)

  implicit val ByteArrayCodec: Codec[Array[Byte]] =
    new NativeArrayCodec[Byte, ByteBuffer](1, b => b, _.put(_), _.get(_))
  implicit val ShortArrayCodec: Codec[Array[Short]] =
    new NativeArrayCodec[Short, ShortBuffer](2, _.asShortBuffer, _.put(_), _.get(_))
  implicit val IntArrayCodec: Codec[Array[Int]] =
    new NativeArrayCodec[Int, IntBuffer](4, _.asIntBuffer, _.put(_), _.get(_))
  implicit val LongArrayCodec: Codec[Array[Long]] =
    new NativeArrayCodec[Long, LongBuffer](8, _.asLongBuffer, _.put(_), _.get(_))
  implicit val ArrayCharCodec: Codec[Array[Char]] =
    new NativeArrayCodec[Char, CharBuffer](2, _.asCharBuffer, _.put(_), _.get(_))
  implicit val FloatArrayCodec: Codec[Array[Float]] =
    new NativeArrayCodec[Float, FloatBuffer](4, _.asFloatBuffer, _.put(_), _.get(_))
  implicit val DoubleArrayCodec: Codec[Array[Double]] =
    new NativeArrayCodec[Double, DoubleBuffer](8, _.asDoubleBuffer, _.put(_), _.get(_))

  implicit val BooleanArrayCodec: Codec[Array[Boolean]] = ByteArrayCodec.map(_.map(byte2bool), _.map(bool2byte))

  implicit val ByteBufferCodec: Codec[ByteBuffer] = ByteArrayCodec.map(toByteBuffer, toArray)

}

object NativeInstances extends NativeInstances

final class NativeCodec[T](val size: Int, w: (ByteBuffer, Int, T) => Unit, r: (ByteBuffer, Int) => T) extends Codec[T] {
  override final def size(t: T): Int = size

  override final def encodeImpl(t: T, start: Int, destination: ByteBuffer): Unit =
    w(destination, start, t)

  final def doDecode(start: Int, source: ByteBuffer): T =
    r(source, start)

  override final def decodeImpl(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[T]] = {
    if (start < 0)
      Left(Failure)
    else {
      (source: java.nio.Buffer).position(start)
      if (size > source.remaining)
        Left(Failure)
      else
        Right(DecodeResult(doDecode(start, source), start + size))
    }
  }
}

final class NativeArrayCodec[T: ClassTag, TB](
    val size: Int,
    m: ByteBuffer => TB,
    w: (TB, Array[T]) => Unit,
    r: (TB, Array[T]) => Unit
)(implicit iCodec: Codec[Int])
    extends Codec[Array[T]] {

  override final def size(t: Array[T]): Int =
    iCodec.size(t.length) + t.length * size

  override final def encodeImpl(t: Array[T], start: Int, destination: ByteBuffer): Unit = {
    val s = iCodec.size(t.length)
    iCodec.encodeImpl(t.length, start, destination)
    (destination: java.nio.Buffer).position(start + s)
    val b = m(destination)
    w(b, t)
  }

  override final def decodeImpl(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[Array[T]]] = {
    if (start < 0)
      Left(Failure)
    else {
      (source: java.nio.Buffer).position(start)
      val is = iCodec.size(0)
      if (is > source.remaining)
        Left(Failure)
      else {
        iCodec.decodeImpl(start, source) flatMap {
          case DecodeResult(s, ni) =>
            (source: java.nio.Buffer).position(ni)
            val l = s * size
            if (l > source.remaining)
              Left(Failure)
            else {
              val b = m(source)
              val result = new Array[T](s)
              r(b, result)
              Right(DecodeResult(result, ni + l))
            }
        }
      }
    }
  }
}
