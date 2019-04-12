package io.iohk.decco
package auto.instances

import java.nio.ByteBuffer

import scala.collection.generic.{CanBuildFrom, IsTraversableLike}
import scala.collection.GenTraversable

object TraversableCodec {
  type IsTraversableLikeAux[AA, R] = IsTraversableLike[R] { type A = AA }
}

import TraversableCodec._

class TraversableCodec[T, CT](
    implicit
    iCodec: Codec[Int],
    tCodec: Codec[T],
    cbf: CanBuildFrom[CT, T, CT],
    itl: IsTraversableLikeAux[T, CT]
) extends Codec[CT] {

  override def encodeImpl(ts: CT, start: Int, destination: ByteBuffer): Unit = {
    val c = asCollection(ts)
    val l = c.size
    val is = iCodec.size(l)
    iCodec.encodeImpl(l, start, destination)
    var nextStart = start + is
    c foreach { elem =>
      tCodec.encodeImpl(elem, nextStart, destination)
      val s = tCodec.size(elem)
      nextStart += s
    }
  }

  override def decodeImpl(start: Int, source: ByteBuffer): Either[Codec.Failure, Codec.DecodeResult[CT]] = {
    iCodec.decodeImpl(start, source) match {
      case Right(Codec.DecodeResult(count, nextStart)) =>
        val builder = cbf()
        builder.sizeHint(count)
        var failed = false
        var nextIndex = nextStart
        var i = 0
        while (!failed && i < count) {
          tCodec.decodeImpl(nextIndex, source) match {
            case Left(Codec.Failure) =>
              failed = true
            case Right(Codec.DecodeResult(e, ni)) =>
              nextIndex = ni
              builder += e
          }
          i += 1
        }
        if (failed) Left(Codec.Failure) else Right(Codec.DecodeResult(builder.result(), nextIndex))
      case Left(_) =>
        Left(Codec.Failure)
    }
  }

  override def size(ts: CT): Int = {
    val c = asCollection(ts)
    val l = c.size
    val is = iCodec.size(l)
    c.foldLeft(is)((acc, next) => {
      val elementSize = tCodec.size(next)
      acc + elementSize
    })
  }

  private def asCollection(ts: CT): GenTraversable[T] = itl.conversion(ts).toTraversable

}
