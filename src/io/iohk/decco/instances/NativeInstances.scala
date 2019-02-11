package io.iohk.decco.instances

import io.iohk.decco.PartialCodec
import io.iohk.decco.PartialCodec.{DecodeResult, Failure, typeTagCode}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

// format: off
trait NativeInstances {

  import NativeInstanceHelpers._

  implicit val BytePartialCodec: PartialCodec[Byte] = new NativePartialCodec[Byte](size = 1) {
    def encode(t: Byte, start: Int, destination: Array[Byte]): Unit =
      destination(start) = t

    def doDecode(start: Int, source: Array[Byte]): Byte =
      source(start)
  }

  implicit val ShortPartialCodec: PartialCodec[Short] = new NativePartialCodec[Short](size = 2) {
    def encode(t: Short, start: Int, destination: Array[Byte]): Unit = {
      destination(start + 0) = (t >> 8).asInstanceOf[Byte]
      destination(start + 1) = (t     ).asInstanceOf[Byte]
    }
    def doDecode(start: Int, source: Array[Byte]): Short = {
      (source(start + 0)          << 8 |
      (source(start + 1) & 0xFF))
      .asInstanceOf[Short]
    }
  }

  implicit val IntPartialCodec: PartialCodec[Int] = new NativePartialCodec[Int](size = 4) {
    def encode(t: Int, start: Int, destination: Array[Byte]): Unit = {
      destination(start + 0) = (t >> 24).asInstanceOf[Byte]
      destination(start + 1) = (t >> 16).asInstanceOf[Byte]
      destination(start + 2) = (t >>  8).asInstanceOf[Byte]
      destination(start + 3) = (t      ).asInstanceOf[Byte]
    }
    def doDecode(start: Int, source: Array[Byte]): Int =
      (source(start + 0)       ) << 24 |
      (source(start + 1) & 0xFF) << 16 |
      (source(start + 2) & 0xFF) <<  8 |
      (source(start + 3) & 0xFF)
  }

  implicit val LongPartialCodec: PartialCodec[Long] = new NativePartialCodec[Long](size = 8) {
    override def encode(l: Long, start: Int, destination: Array[Byte]): Unit = {
      destination(start + 0) = (l >> 56).asInstanceOf[Byte]
      destination(start + 1) = (l >> 48).asInstanceOf[Byte]
      destination(start + 2) = (l >> 40).asInstanceOf[Byte]
      destination(start + 3) = (l >> 32).asInstanceOf[Byte]
      destination(start + 4) = (l >> 24).asInstanceOf[Byte]
      destination(start + 5) = (l >> 16).asInstanceOf[Byte]
      destination(start + 6) = (l >>  8).asInstanceOf[Byte]
      destination(start + 7) = (l      ).asInstanceOf[Byte]
    }

    override def doDecode(start: Int, source: Array[Byte]): Long = {
      (source(start + 0)       ).asInstanceOf[Long] << 56 |
      (source(start + 1) & 0xFF).asInstanceOf[Long] << 48 |
      (source(start + 2) & 0xFF).asInstanceOf[Long] << 40 |
      (source(start + 3) & 0xFF).asInstanceOf[Long] << 32 |
      (source(start + 4) & 0xFF).asInstanceOf[Long] << 24 |
      (source(start + 5) & 0xFF).asInstanceOf[Long] << 16 |
      (source(start + 6) & 0xFF).asInstanceOf[Long] <<  8 |
      (source(start + 7) & 0xFF).asInstanceOf[Long]
    }
  }

  implicit val BooleanPartialCodec: PartialCodec[Boolean] = new NativePartialCodec[Boolean](size = 1) {
    def encode(b: Boolean, start: Int, destination: Array[Byte]): Unit =
      destination(start) = if (b) 1 else 0

    def doDecode(start: Int, source: Array[Byte]): Boolean =
      source(start) == 1
  }

  implicit val CharPartialCodec: PartialCodec[Char] = new NativePartialCodec[Char](size = 2) {

    override def doDecode(start: Int, source: Array[Byte]): Char = {
      ((source(start + 0) << 8) |
      (source(start + 1) & 0xff)).asInstanceOf[Char]
    }

    override def encode(c: Char, start: Int, destination: Array[Byte]): Unit = {
      destination(start + 0) = (c >> 8).asInstanceOf[Byte]
      destination(start + 1) = (c     ).asInstanceOf[Byte]
    }
  }

  implicit val FloatPartialCodec: PartialCodec[Float] = new NativePartialCodec[Float](size = 4) {
    override def encode(f: Float, start: Int, destination: Array[Byte]): Unit =
      IntPartialCodec.encode(java.lang.Float.floatToRawIntBits(f), start, destination)

    override def doDecode(start: Int, source: Array[Byte]): Float =
      java.lang.Float.intBitsToFloat(IntPartialCodec.asInstanceOf[NativePartialCodec[Int]].doDecode(start, source))
  }

  implicit val DoublePartialCodec: PartialCodec[Double] = new NativePartialCodec[Double](8) {
    override def encode(d: Double, start: Int, destination: Array[Byte]): Unit =
      LongPartialCodec.encode(java.lang.Double.doubleToRawLongBits(d), start, destination)

    override def doDecode(start: Int, source: Array[Byte]): Double =
      java.lang.Double.longBitsToDouble(LongPartialCodec.asInstanceOf[NativePartialCodec[Long]].doDecode(start, source))
  }

  implicit val ByteArrayPartialCodec: PartialCodec[Array[Byte]] =
    buildNativeArrayCodec[Byte]

  implicit val ShortArrayPartialCodec: PartialCodec[Array[Short]] =
    buildNativeArrayCodec[Short]

  implicit val IntArrayPartialCodec: PartialCodec[Array[Int]] =
    buildNativeArrayCodec[Int]

  implicit val LongArrayPartialCodec: PartialCodec[Array[Long]] =
    buildNativeArrayCodec[Long]

  implicit val FloatArrayPartialCodec: PartialCodec[Array[Float]] =
    buildNativeArrayCodec[Float]

  implicit val DoubleArrayPartialCodec: PartialCodec[Array[Double]] =
    buildNativeArrayCodec[Double]

  implicit val BooleanArrayPartialCodec: PartialCodec[Array[Boolean]] =
    buildNativeArrayCodec[Boolean]

  implicit val CharArrayPartialCodec: PartialCodec[Array[Char]] =
    buildNativeArrayCodec[Char](ClassTag.Char, TypeTag.Char, IntPartialCodec, CharPartialCodec)

  implicit val StringPartialCodec: PartialCodec[String] =
    CharArrayPartialCodec.map[String](typeTagCode[String], String.copyValueOf, _.toCharArray)

}

object NativeInstanceHelpers {

  abstract class NativePartialCodec[T: TypeTag](val size: Int) extends PartialCodec[T] {
    override def size(t: T): Int = size

    override def typeCode: String = typeTagCode[T]

    def doDecode(start: Int, source: Array[Byte]): T

    override def decode(start: Int, source: Array[Byte]): Either[Failure, DecodeResult[T]] = {
      if (start < 0 || start + size > source.length)
        Left(Failure)
      else
        Right(DecodeResult(doDecode(start, source), start + size))
    }
  }

  def buildNativeArrayCodec[T: ClassTag : TypeTag](
                                                             implicit iCodec: PartialCodec[Int],
                                                             tCodec: PartialCodec[T]
                                                           ): PartialCodec[Array[T]] =
    new PartialCodec[Array[T]] {

      def size(ts: Array[T]): Int = iCodec.size(ts.length) + pureArraySize(ts)

      def decode(start: Int, source: Array[Byte]): Either[Failure, DecodeResult[Array[T]]] = {
        iCodec.decode(start, source) match {
          case Right(DecodeResult(count, nextIndex)) =>
            pureArrayDecode(nextIndex, source, count)
          case Left(Failure) =>
            Left(Failure)
        }
      }

      def encode(ts: Array[T], start: Int, destination: Array[Byte]): Unit = {
        iCodec.encode(ts.length, start, destination)
        pureArrayEncode(ts, start + iCodec.size(ts.length), destination)
      }

      override val typeCode: String = typeTagCode[Array[T]]
    }

  private def pureArraySize[T](ts: Array[T])(implicit tCodec: PartialCodec[T]): Int =
    ts.headOption match {
      case None => 0
      case Some(h) => tCodec.size(h) * ts.length
    }

  private def pureArrayEncode[T](ts: Array[T], start: Int, destination: Array[Byte])(
    implicit tCodec: PartialCodec[T]
  ): Unit = {

    if (ts.length != 0) {
      val elementSize = tCodec.size(ts.head)

      ts.foldLeft(start)((iDest, t: T) => {
        tCodec.encode(t, iDest, destination)
        iDest + elementSize
      })
    }
  }

  private def pureArrayDecode[T: ClassTag](start: Int, source: Array[Byte], count: Int)(
    implicit tCodec: PartialCodec[T]
  ): Either[Failure, DecodeResult[Array[T]]] = {
    val r = new Array[T](count)
    var i = 0
    var j = start
    while (i < count) {
      tCodec.decode(j, source) match {
        case Right(DecodeResult(t, nextJ)) =>
          r(i) = t
          j = nextJ
        case Left(Failure) =>
          return Left(Failure)
      }
      i += 1
    }
    Right(DecodeResult(r, j))
  }
}

// format: on
object NativeInstances extends NativeInstances
