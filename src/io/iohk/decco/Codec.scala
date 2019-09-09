package io.iohk.decco

import java.nio.ByteBuffer

trait Codec[T] {

  import Codec.Failure

  def encode[B](t: T)(implicit bi: BufferInstantiator[B]): B

  def decode[B](start: Int, source: B)(implicit bi: BufferInstantiator[B]): Either[Failure, T]

  def decode[B](source: B)(implicit bi: BufferInstantiator[B]): Either[Failure, T]
}

object Codec {

  case class DecodeResult[+T](decoded: T, nextIndex: Int)
  case object Failure
  type Failure = Failure.type

  def apply[T](implicit ev: Codec[T]): Codec[T] = ev

}

trait CodecContract[T] { self =>

  import Codec._

  def size(t: T): Int
  def encodeImpl(t: T, start: Int, destination: ByteBuffer): Unit
  def decodeImpl(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[T]]

  final def encode[B](t: T)(implicit bi: BufferInstantiator[B]): B = {
    val bb = bi.instantiateByteBuffer(size(t))
    encodeImpl(t, 0, bb)
    (bb: java.nio.Buffer).position(0)
    bi.asB(bb)
  }

  final def decode[B](start: Int, source: B)(implicit bi: BufferInstantiator[B]): Either[Failure, T] = {
    val bb = bi.asByteBuffer(source)
    decodeImpl(start, bb).map(_.decoded)
  }

  final def decode[B](source: B)(implicit bi: BufferInstantiator[B]): Either[Failure, T] =
    decode(0, source)

  // SOME COMPOSITION FUNCTIONS

  final def zip[U](that: CodecContract[U]): CodecContract[(T, U)] = new CodecContract[(T, U)] {

    def size(tu: (T, U)): Int = tu match {
      case (t, u) => self.size(t) + that.size(u)
    }

    def decodeImpl(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[(T, U)]] = {
      self.decodeImpl(start, source) match {
        case Left(Failure) =>
          Left(Failure)
        case Right(DecodeResult(t, nextIndex)) =>
          that.decodeImpl(nextIndex, source) match {
            case Left(Failure) =>
              Left(Failure)
            case Right(DecodeResult(u, resultingNextIndex)) =>
              Right(DecodeResult((t, u), resultingNextIndex))
          }
      }
    }

    def encodeImpl(tuple: (T, U), start: Int, destination: ByteBuffer): Unit = {
      val (t, u) = tuple
      val tSize = self.size(t)
      self.encodeImpl(t, start, destination)
      that.encodeImpl(u, start + tSize, destination)
    }
  }

  final def map[U](t2u: T => U, u2t: U => T): CodecContract[U] = new CodecContract[U] {
    def size(u: U): Int = self.size(u2t(u))
    def decodeImpl(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[U]] =
      self.decodeImpl(start, source) match {
        case Left(Failure) =>
          Left(Failure)
        case Right(DecodeResult(t, nextIndex)) =>
          Right(DecodeResult(t2u(t), nextIndex))
      }

    def encodeImpl(u: U, start: Int, destination: ByteBuffer): Unit =
      self.encodeImpl(u2t(u), start, destination)
  }

  final def mapOpt[U](t2u: T => Option[U], u2t: U => T): CodecContract[U] = new CodecContract[U] {
    def size(u: U): Int = self.size(u2t(u))
    def decodeImpl(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[U]] =
      self.decodeImpl(start, source) match {
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

    def encodeImpl(u: U, start: Int, destination: ByteBuffer): Unit =
      self.encodeImpl(u2t(u), start, destination)
  }

}
