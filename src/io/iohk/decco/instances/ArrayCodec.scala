package io.iohk.decco.instances

import io.iohk.decco.PartialCodec
import io.iohk.decco.PartialCodec.{DecodeResult, Failure, typeTagCode}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

class ArrayCodec[T: ClassTag: TypeTag](implicit iCodec: PartialCodec[Int], tCodec: PartialCodec[T])
    extends PartialCodec[Array[T]] {

  def size(ts: Array[T]): Int = iCodec.size(ts.length) + arraySize(ts)

  def decode(start: Int, source: Array[Byte]): Either[Failure, DecodeResult[Array[T]]] = {
    iCodec.decode(start, source) match {
      case Right(DecodeResult(count, nextIndex)) =>
        arrayDecode(nextIndex, source, count)
      case Left(Failure) =>
        Left(Failure)
    }
  }

  def encode(ts: Array[T], start: Int, destination: Array[Byte]): Unit = {
    iCodec.encode(ts.length, start, destination)
    arrayEncode(ts, start + iCodec.size(ts.length), destination)
  }

  override val typeCode: String = typeTagCode[Array[T]]

  private def arraySize(ts: Array[T])(implicit tCodec: PartialCodec[T]): Int =
    ts.foldLeft(0)((acc, next) => {
      val elementSize = tCodec.size(next)
      acc + elementSize
    })

  private def arrayEncode(ts: Array[T], start: Int, destination: Array[Byte])(
      implicit tCodec: PartialCodec[T]
  ): Unit = {
    ts.foldLeft(start)((iDest, t: T) => {
      val elementSize = tCodec.size(t)
      tCodec.encode(t, iDest, destination)
      iDest + elementSize
    })
  }

  private def arrayDecode(start: Int, source: Array[Byte], count: Int)(
      implicit tCodec: PartialCodec[T]
  ): Either[Failure, DecodeResult[Array[T]]] = {

    def nextDecode(
        iDest: Int,
        accE: Either[Failure, DecodeResult[Array[T]]]
    ): Either[Failure, DecodeResult[Array[T]]] = {
      if (iDest == count)
        accE
      else
        accE.flatMap { acc =>
          tCodec.decode(acc.nextIndex, source).flatMap { tResult =>
            val dest: Array[T] = acc.decoded
            dest(iDest) = tResult.decoded

            nextDecode(iDest + 1, Right(DecodeResult(dest, tResult.nextIndex)))
          }
        }
    }

    nextDecode(0, Right(DecodeResult(new Array[T](count), start)))
  }
}
