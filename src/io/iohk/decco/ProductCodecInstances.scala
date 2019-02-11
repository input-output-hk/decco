package io.iohk.decco

import io.iohk.decco.PartialCodec.{DecodeResult, Failure, typeTagCode}
import shapeless.{::, Generic, HList, HNil, Lazy}

trait ProductCodecInstances {

  implicit val hNilPC: PartialCodec[HNil] = new PartialCodec[HNil] {
    override def size(t: HNil): Int = 0

    override val typeCode: String = typeTagCode[HNil]

    override def encode(t: HNil, start: Int, destination: Array[Byte]): Unit = ()

    override def decode(start: Int, source: Array[Byte]): Either[Failure, DecodeResult[HNil]] =
      Right(DecodeResult(HNil, start))
  }

  implicit def hListPC[H, T <: HList](implicit hPc: Lazy[PartialCodec[H]], tPc: PartialCodec[T]): PartialCodec[H :: T] =
    new PartialCodec[H :: T] {
      override def size(ht: H :: T): Int = ht match {
        case h :: t =>
          hPc.value.size(h) + tPc.size(t)
      }

      override val typeCode: String = s"${hPc.value.typeCode} shapeless.:: ${tPc.typeCode}"

      override def encode(ht: H :: T, start: Int, destination: Array[Byte]): Unit = ht match {
        case h :: t =>
          val hSz = hPc.value.size(h)
          hPc.value.encode(h, start, destination)
          tPc.encode(t, start + hSz, destination)
      }

      override def decode(start: Int, source: Array[Byte]): Either[Failure, DecodeResult[H :: T]] = {
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

  implicit def genericPC[T, R](implicit gen: Generic.Aux[T, R], enc: Lazy[PartialCodec[R]]): PartialCodec[T] = {
    enc.value.map[T](s"shapeless.Generic(${enc.value.typeCode}", gen.from, gen.to)
  }
}

object ProductCodecInstances extends ProductCodecInstances
