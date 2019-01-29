package io.iohk.cef.decco

import scala.reflect.ClassTag

package object auto {

  implicit val BytePartialCodec: PartialCodec[Byte] = new PartialCodec[Byte] {

    def size(t: Byte): Int = 1

    def decode(start: Int, source: Array[Byte]): DecodeResult[Byte] =
      if (start >= 0 && start < source.length)
        DecodeResult.Success(source(start), start + 1)
      else
        DecodeResult.Failure

    def encode(t: Byte, start: Int, destination: Array[Byte]): EncodeResult =
      if (start < 0 )
        EncodeResult.NegativeIndex
      else if (start >= destination.size)
        EncodeResult.NotEnoughStorage
      else {
        destination(start) = t
        EncodeResult.EncodeSuccess
      }

  }

  implicit val IntPartialCodec: PartialCodec[Int] = new PartialCodec[Int] {

    def size(t: Int): Int = 4

    def decode(start: Int, source: Array[Byte]): io.iohk.cef.decco.DecodeResult[Int] = {

      if (start < 0 || start + 4 >= source.size)
        DecodeResult.Failure
      else {
        val r =
          (source(start + 0)       ) << 24 |
          (source(start + 1) & 0xFF) << 16 |
          (source(start + 2) & 0xFF) <<  8 |
          (source(start + 3) & 0xFF)
        DecodeResult.Success(r, start + 4)
      }

    }

    def encode(t: Int, start: Int, destination: Array[Byte]): EncodeResult =
      if(start < 0)
        EncodeResult.NegativeIndex
      else if (start + 4 >= destination.length)
        EncodeResult.NotEnoughStorage
      else {

        destination(start + 0) = (t >> 24).asInstanceOf[Byte]
        destination(start + 1) = (t >> 16).asInstanceOf[Byte]
        destination(start + 2) = (t >>  8).asInstanceOf[Byte]
        destination(start + 3) = (t      ).asInstanceOf[Byte]

        EncodeResult.EncodeSuccess
      }
  }

  implicit val IntArrayPartialCodec: PartialCodec[Array[Int]] =
    buildNativeArrayCodec[Int]

  implicit val ByteArrayPartialCodec: PartialCodec[Array[Byte]] =
    buildNativeArrayCodec[Byte]

  implicit val StringPartialCodec: PartialCodec[String] =
    ByteArrayPartialCodec.map[String](bs => new String(bs, "UTF-8"), s => s.getBytes("UTF-8"))

  // UTILS

  private def buildNativeArrayCodec[T: ClassTag](implicit iCodec: PartialCodec[Int], tCodec: PartialCodec[T]): PartialCodec[Array[T]] =
    new PartialCodec[Array[T]] {

      def size(ts: Array[T]): Int = iCodec.size(ts.length) + pureArraySize(ts)

      def decode(start: Int, source: Array[Byte]): DecodeResult[Array[T]] = {
        iCodec.decode(start, source) match {
          case DecodeResult.Success(count, nextIndex) =>
            pureArrayDecode(nextIndex, source, count)
          case DecodeResult.Failure =>
            DecodeResult.Failure
        }
      }

      def encode(ts: Array[T], start: Int, destination: Array[Byte]): EncodeResult =
        iCodec.encode(ts.length, start, destination) match {
          case EncodeResult.EncodeSuccess =>
            pureArrayEncode(ts, start + iCodec.size(ts.length), destination)
          case otherwise =>
            otherwise
        }

  }

  private def pureArraySize[T](ts: Array[T])(implicit tCodec: PartialCodec[T]): Int =
    ts.headOption match {
      case None => 0
      case Some(h) => tCodec.size(h) * ts.length
    }

  private def pureArrayEncode[T](ts: Array[T], start: Int, destination: Array[Byte])(implicit tCodec: PartialCodec[T]): EncodeResult =
    if (start < 0)
      EncodeResult.NegativeIndex
    else
      ts.headOption match {
        case None => EncodeResult.EncodeSuccess // The Array is empty, nothing to encode
        case Some(h) =>
          val elementSize = tCodec.size(h)
          if (start + elementSize * ts.length >= destination.length)
            EncodeResult.NotEnoughStorage
          else {
            var i = 0
            var j = start
            while (i < ts.length) {
              val t = ts(i)
              tCodec.encode(t, j, destination) match {
                case EncodeResult.EncodeSuccess => // All OK, continue with the encoding
                case someError => return someError // Fail Fast
              }
              j += elementSize
              i += 1
            }

            EncodeResult.EncodeSuccess
          }
      }

  private def pureArrayDecode[T: ClassTag](start: Int, source: Array[Byte], count: Int)(implicit tCodec: PartialCodec[T]): DecodeResult[Array[T]] = {
    val r = new Array[T](count)
    var i = 0
    var j = start
    while (i < count) {
      tCodec.decode(j, source) match {
        case DecodeResult.Success(t, nextJ) =>
          r(i) = t
          j = nextJ
        case DecodeResult.Failure =>
          return DecodeResult.Failure
      }
      i += 1
    }
    DecodeResult.Success(r, j)
  }
}
