package io.iohk.decco.instances

import java.nio.ByteBuffer

import io.iohk.decco.PartialCodec
import io.iohk.decco.PartialCodec.{DecodeResult, Failure}

import scala.collection.generic.{CanBuildFrom, IsTraversableLike}
import scala.collection.{GenTraversable, mutable}

object TraversableCodec {
  type IsTraversableLikeAux[AA, R] = IsTraversableLike[R] { type A = AA }
}

import io.iohk.decco.instances.TraversableCodec._

class TraversableCodec[T, CT](
    implicit
    iCodec: PartialCodec[Int],
    tCodec: PartialCodec[T],
    cbf: CanBuildFrom[CT, T, CT],
    itl: IsTraversableLikeAux[T, CT]
) extends PartialCodec[CT] {

  override def encode(ts: CT, start: Int, destination: ByteBuffer): Unit = {
    encodeHeaderAndBody(toGenTraversable(ts), start, destination)
  }

  override def decode(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[CT]] = {
    iCodec.decode(start, source) match {
      case Right(DecodeResult(count, nextIndex)) =>
        decodeBody(nextIndex, source, count)
      case Left(Failure) =>
        Left(Failure)
    }
  }

  override def size(ts: CT): Int = {
    val gt = toGenTraversable(ts)
    iCodec.size(gt.size) + bodySize(gt, tCodec)
  }

  private def toGenTraversable(ts: CT): GenTraversable[T] = itl.conversion(ts).toTraversable

  private def bodySize(ts: GenTraversable[T], tCodec: PartialCodec[T]): Int =
    ts.foldLeft(0)((acc, next) => {
      val elementSize = tCodec.size(next)
      acc + elementSize
    })

  private def encodeHeaderAndBody(ts: GenTraversable[T], start: Int, destination: ByteBuffer)(
      implicit tCodec: PartialCodec[T]
  ): Unit = {

    val sizeHeader = ts.size

    iCodec.encode(sizeHeader, start, destination)

    ts.foldLeft(start + iCodec.size(sizeHeader))((iDest, t: T) => {
      val elementSize = tCodec.size(t)
      tCodec.encode(t, iDest, destination)
      iDest + elementSize
    })
  }

  private def decodeBody(start: Int, source: ByteBuffer, count: Int)(
      implicit tCodec: PartialCodec[T]
  ): Either[Failure, DecodeResult[CT]] = {

    val dest: mutable.Builder[T, CT] = cbf()
    dest.sizeHint(count)
    var i = 0
    var nextStart = start
    while (i < count) {
      tCodec.decode(nextStart, source) match {
        case Left(error) => return Left(error)
        case Right(DecodeResult(item, ni)) =>
          nextStart = ni
          dest += item
      }
      i += 1
    }
    Right(DecodeResult(dest.result(), nextStart))
  }
}
