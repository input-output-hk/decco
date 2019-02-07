package io.iohk.decco

import io.iohk.decco.EncodeResult.EncodeSuccess
import io.iohk.decco.PartialCodec.typeTagCode
import shapeless.{::, Generic, HList, HNil, Lazy}

trait Products {

  implicit val hNilPC: PartialCodec[HNil] = new PartialCodec[HNil] {
    override def size(t: HNil): Int = 0

    override val typeCode: String = typeTagCode[HNil]

    override def encode(t: HNil, start: Int, destination: Array[Byte]): EncodeResult =
      EncodeResult.EncodeSuccess

    override def decode(start: Int, source: Array[Byte]): DecodeResult[HNil] =
      DecodeResult.Success(HNil, start)
  }

  implicit def hListPC[H, T <: HList](implicit hPc: Lazy[PartialCodec[H]], tPc: PartialCodec[T]): PartialCodec[H :: T] = new PartialCodec[H :: T] {
    override def size(ht: H :: T): Int = ht match {
      case h :: t =>
        hPc.value.size(h) + tPc.size(t)
    }

    override val typeCode: String = hPc.value.typeCode + "::" + tPc.typeCode

    override def encode(ht: H :: T, start: Int, destination: Array[Byte]): EncodeResult = ht match {
      case h :: t =>
        val hSz = hPc.value.size(h)
        val eh: EncodeResult = hPc.value.encode(h, start, destination)
        eh match {
          case EncodeSuccess =>
            tPc.encode(t, start + hSz, destination)
          case otherwise =>
            otherwise
        }
    }

    override def decode(start: Int, source: Array[Byte]): DecodeResult[H :: T] = {
      hPc.value.decode(start, source) match {
        case DecodeResult.Success(h, nextIndex) =>
          tPc.decode(nextIndex, source) match {
            case DecodeResult.Success(t, nextNextIndex) =>
              DecodeResult.Success(h :: t, nextNextIndex)
            case DecodeResult.Failure =>
              DecodeResult.Failure
          }
        case DecodeResult.Failure =>
          DecodeResult.Failure
      }
    }
  }

  implicit def genericPC[T, R](implicit gen: Generic.Aux[T, R], enc: Lazy[PartialCodec[R]]): PartialCodec[T] = {
    enc.value.map[T]("shapeless.Generic" + enc.value.typeCode, gen.from, gen.to)
  }
}
