package io.iohk.decco

import io.iohk.decco.PartialCodec.{DecodeResult, Failure, typeTagCode}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

trait NativeCodecInstances {

  implicit val BytePartialCodec: PartialCodec[Byte] = new PartialCodec[Byte] {

    def size(t: Byte): Int = 1

    def decode(start: Int, source: Array[Byte]): Either[Failure, DecodeResult[Byte]] =
      if (start >= 0 && start < source.length)
        Right(DecodeResult(source(start), start + 1))
      else
        Left(Failure)

    def encode(t: Byte, start: Int, destination: Array[Byte]): Unit =
      destination(start) = t

    override val typeCode: String = typeTagCode[Byte]
  }

  implicit val IntPartialCodec: PartialCodec[Int] = new PartialCodec[Int] {

    def size(t: Int): Int = 4

    def decode(start: Int, source: Array[Byte]): Either[Failure, DecodeResult[Int]] = {

      if (start < 0 || start + 4 >= source.length)
        Left(Failure)
      else {
        val r =
          (source(start + 0)) << 24 |
            (source(start + 1) & 0xFF) << 16 |
            (source(start + 2) & 0xFF) << 8 |
            (source(start + 3) & 0xFF)
        Right(DecodeResult(r, start + 4))
      }

    }

    def encode(t: Int, start: Int, destination: Array[Byte]): Unit = {
      destination(start + 0) = (t >> 24).asInstanceOf[Byte]
      destination(start + 1) = (t >> 16).asInstanceOf[Byte]
      destination(start + 2) = (t >> 8).asInstanceOf[Byte]
      destination(start + 3) = t.asInstanceOf[Byte]
    }

    override val typeCode: String = typeTagCode[Int]
  }

  implicit val IntArrayPartialCodec: PartialCodec[Array[Int]] =
    buildNativeArrayCodec[Int]

  implicit val ByteArrayPartialCodec: PartialCodec[Array[Byte]] =
    buildNativeArrayCodec[Byte]

  implicit val StringPartialCodec: PartialCodec[String] =
    ByteArrayPartialCodec.map[String](typeTagCode[String], bs => new String(bs, "UTF-8"), s => s.getBytes("UTF-8"))

  // UTILS

  private def buildNativeArrayCodec[T: ClassTag: TypeTag](
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

object NativeCodecInstances extends NativeCodecInstances
