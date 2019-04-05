package io.iohk.decco

import java.nio.ByteBuffer

trait PartialCodec[T] { self =>

  import PartialCodec._

  def size(t: T): Int
  def encode(t: T, start: Int, destination: ByteBuffer): Unit
  def decode(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[T]]

  // SOME COMPOSITION FUNCTIONS

  final def zip[U](that: PartialCodec[U]): PartialCodec[(T, U)] = new PartialCodec[(T, U)] {

    def size(tu: (T, U)): Int = tu match {
      case (t, u) => self.size(t) + that.size(u)
    }

    def decode(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[(T, U)]] = {
      self.decode(start, source) match {
        case Left(Failure) =>
          Left(Failure)
        case Right(DecodeResult(t, nextIndex)) =>
          that.decode(nextIndex, source) match {
            case Left(Failure) =>
              Left(Failure)
            case Right(DecodeResult(u, resultingNextIndex)) =>
              Right(DecodeResult((t, u), resultingNextIndex))
          }
      }
    }

    def encode(tuple: (T, U), start: Int, destination: ByteBuffer): Unit = {
      val (t, u) = tuple
      val tSize = self.size(t)
      self.encode(t, start, destination)
      that.encode(u, start + tSize, destination)
    }
  }

  final def map[U](t2u: T => U, u2t: U => T): PartialCodec[U] = new PartialCodec[U] {
    def size(u: U): Int = self.size(u2t(u))
    def decode(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[U]] =
      self.decode(start, source) match {
        case Left(Failure) =>
          Left(Failure)
        case Right(DecodeResult(t, nextIndex)) =>
          Right(DecodeResult(t2u(t), nextIndex))
      }

    def encode(u: U, start: Int, destination: ByteBuffer): Unit =
      self.encode(u2t(u), start, destination)
  }

  final def mapOpt[U](t2u: T => Option[U], u2t: U => T): PartialCodec[U] = new PartialCodec[U] {
    def size(u: U): Int = self.size(u2t(u))
    def decode(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[U]] =
      self.decode(start, source) match {
        case Left(Failure) =>
          Left(Failure)
        case Right(DecodeResult(t, nextIndex)) =>
          t2u(t) match {
            case None =>
              Left(Failure)
            case Some(u) =>
              Right(DecodeResult(u, nextIndex))
          }
      }

    def encode(u: U, start: Int, destination: ByteBuffer): Unit =
      self.encode(u2t(u), start, destination)
  }

}

object PartialCodec {

  import scala.reflect.runtime.universe.TypeTag

  case class DecodeResult[+T](decoded: T, nextIndex: Int)
  case object Failure
  type Failure = Failure.type

  def apply[T](implicit ev: PartialCodec[T]): PartialCodec[T] = ev

  def typeTagCode[T](implicit typeTag: TypeTag[T]): String =
    typeTag.toString()
}
