package io.iohk.decco
package auto.instances

import java.nio.ByteBuffer

import Codec.{DecodeResult, Failure}
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}
import java.security.MessageDigest
import scala.reflect.ClassTag

trait ProductInstances {

  implicit val hNilPC: CodecContract[HNil] = new CodecContract[HNil] {
    override def size(t: HNil): Int = 0

    override def encodeImpl(t: HNil, start: Int, destination: ByteBuffer): Unit = ()

    override def decodeImpl(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[HNil]] =
      Right(DecodeResult(HNil, start))
  }

  implicit def hConsPC[H, T <: HList](
      implicit hPc: Lazy[CodecContract[H]],
      tPc: CodecContract[T]
  ): CodecContract[H :: T] =
    new CodecContract[H :: T] {
      override def size(ht: H :: T): Int = ht match {
        case h :: t =>
          hPc.value.size(h) + tPc.size(t)
      }

      override def encodeImpl(ht: H :: T, start: Int, destination: ByteBuffer): Unit = ht match {
        case h :: t =>
          val hSz = hPc.value.size(h)
          hPc.value.encodeImpl(h, start, destination)
          tPc.encodeImpl(t, start + hSz, destination)
      }

      override def decodeImpl(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[H :: T]] = {
        hPc.value.decodeImpl(start, source) match {
          case Right(DecodeResult(h, nextIndex)) =>
            tPc.decodeImpl(nextIndex, source) match {
              case Right(DecodeResult(t, nextNextIndex)) =>
                Right(DecodeResult(h :: t, nextNextIndex))
              case Left(Failure) =>
                Left(Failure)
            }
          case Left(Failure) =>
            Left(Failure)
        }
      }
    }

  implicit def cNilPC: CodecContract[CNil] =
    throw new UnsupportedOperationException("CNil decoding not defined")

  implicit def cUnionPC[H: ClassTag, T <: Coproduct](
      implicit hPc: Lazy[CodecContract[H]],
      tPc: Lazy[CodecContract[T]]
  ): CodecContract[H :+: T] = new CodecContract[H :+: T] {

    override def size(ht: H :+: T): Int = ht match {
      case Inl(h) => hPc.value.size(h)
      case Inr(t) => tPc.value.size(t)
    }

    override def encodeImpl(ht: H :+: T, start: Int, destination: ByteBuffer): Unit = ht match {
      case Inl(h) =>
        hPc.value.encodeImpl(h, start, destination)
      case Inr(t) =>
        tPc.value.encodeImpl(t, start, destination)
    }

    override def decodeImpl(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[H :+: T]] = {
      hPc.value.decodeImpl(start, source) match {
        case Right(DecodeResult(hr, hni)) =>
          Right(DecodeResult(Inl(hr), hni))
        case Left(_) =>
          tPc.value.decodeImpl(start, source).map(tResult => DecodeResult(Inr(tResult.decoded), tResult.nextIndex))
      }
    }
  }

  implicit def genericCoproduct[T, R <: Coproduct](
      implicit gen: Generic.Aux[T, R],
      enc: Lazy[CodecContract[R]]
  ): CodecContract[T] = {
    val cd = enc.value
    new CodecContract[T] {
      override def size(t: T): Int = cd.size(gen.to(t))
      override def encodeImpl(t: T, start: Int, destination: ByteBuffer): Unit = {
        cd.encodeImpl(gen.to(t), start, destination)
      }
      override def decodeImpl(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[T]] = {
        cd.decodeImpl(start, source).map { r =>
          r.copy(decoded = gen.from(r.decoded))
        }
      }
    }
  }

  implicit def genericProduct[T: ClassTag, R <: HList](
      implicit gen: Generic.Aux[T, R],
      enc: Lazy[CodecContract[R]]
  ): CodecContract[T] = {
    val code = typeCode[T]
    val cd = enc.value
    new CodecContract[T] {
      override def size(t: T): Int = code.length + cd.size(gen.to(t))
      override def encodeImpl(t: T, start: Int, destination: ByteBuffer): Unit = {
        (destination: java.nio.Buffer).position(start)
        destination.put(code)
        cd.encodeImpl(gen.to(t), start + code.length, destination)
      }
      override def decodeImpl(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[T]] = {
        (source: java.nio.Buffer).position(start)
        if (source.remaining() < code.length) {
          Left(Failure)
        } else {
          val code2 = new Array[Byte](code.length)
          source.get(code2)
          if (code2.deep == code.deep)
            cd.decodeImpl(start + code.length, source).map { r =>
              r.copy(decoded = gen.from(r.decoded))
            } else {
            Left(Failure)
          }
        }
      }
    }
  }

  private def typeCode[T](implicit tt: ClassTag[T]): Array[Byte] = {
    hash(tt.toString())
  }

  private def hash(s: String): Array[Byte] = {
    MessageDigest.getInstance("MD5").digest(s.getBytes)
  }
}

object ProductInstances extends ProductInstances
