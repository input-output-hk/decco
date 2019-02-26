package io.iohk.decco.instances

import java.nio.ByteBuffer

import io.iohk.decco.{PartialCodec, TypeCode}
import io.iohk.decco.PartialCodec.{DecodeResult, Failure}
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}
import scala.reflect.runtime.universe.TypeTag

trait ProductInstances {

  implicit val hNilPC: PartialCodec[HNil] = new PartialCodec[HNil] {
    override def size(t: HNil): Int = 0

    override def encode(t: HNil, start: Int, destination: ByteBuffer): Unit = ()

    override def decode(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[HNil]] =
      Right(DecodeResult(HNil, start))
  }

  implicit def hConsPC[H, T <: HList](implicit hPc: Lazy[PartialCodec[H]], tPc: PartialCodec[T]): PartialCodec[H :: T] =
    new PartialCodec[H :: T] {
      override def size(ht: H :: T): Int = ht match {
        case h :: t =>
          hPc.value.size(h) + tPc.size(t)
      }

      override def encode(ht: H :: T, start: Int, destination: ByteBuffer): Unit = ht match {
        case h :: t =>
          val hSz = hPc.value.size(h)
          hPc.value.encode(h, start, destination)
          tPc.encode(t, start + hSz, destination)
      }

      override def decode(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[H :: T]] = {
        hPc.value.decode(start, source) match {
          case Right(DecodeResult(h, nextIndex)) =>
            tPc.decode(nextIndex, source) match {
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

  implicit val cNilPC: PartialCodec[CNil] = new PartialCodec[CNil] {
    override def size(t: CNil): Int = 0
    override def encode(t: CNil, start: Int, destination: ByteBuffer): Unit =
      throw new UnsupportedOperationException("CNil encoding not defined")
    override def decode(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[CNil]] =
      Left(Failure)
  }

  implicit def cUnionPC[H, T <: Coproduct](
      implicit hPc: Lazy[PartialCodec[H]],
      tPc: PartialCodec[T],
      booleanPc: PartialCodec[Boolean]
  ): PartialCodec[H :+: T] = new PartialCodec[H :+: T] {

    override def size(ht: H :+: T): Int = ht match {
      case Inl(h) => booleanPc.size(true) + hPc.value.size(h)
      case Inr(t) => booleanPc.size(false) + tPc.size(t)
    }

    override def encode(ht: H :+: T, start: Int, destination: ByteBuffer): Unit = ht match {
      case Inl(h) =>
        booleanPc.encode(true, start, destination)
        hPc.value.encode(h, start + booleanPc.size(true), destination)
      case Inr(t) =>
        booleanPc.encode(false, start, destination)
        tPc.encode(t, start + booleanPc.size(false), destination)
    }

    override def decode(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[H :+: T]] = {

      booleanPc.decode(start, source).flatMap { r1: DecodeResult[Boolean] =>
        if (r1.decoded) { // it's l
          hPc.value.decode(r1.nextIndex, source).map(hResult => DecodeResult(Inl(hResult.decoded), hResult.nextIndex))
        } else // it's r
          tPc.decode(r1.nextIndex, source).map(tResult => DecodeResult(Inr(tResult.decoded), tResult.nextIndex))
      }
    }
  }

  implicit def typeCode4TypeTag[T](implicit t: TypeTag[T]): TypeCode[T] = {
    TypeCode(t.tpe.toString)
  }

  implicit def genericPC[T, R](
      implicit gen: Generic.Aux[T, R],
      enc: Lazy[PartialCodec[R]]
  ): PartialCodec[T] = {
    enc.value.map[T](gen.from, gen.to)
  }
}

object ProductInstances extends ProductInstances
