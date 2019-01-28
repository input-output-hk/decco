package io.iohk.cef.decco

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

  // This is a marker trait, indicating that a codec for the pure array should not encode the size of the array when writting it
  final case class PureArray[T](ts: Array[T])

  implicit def PureArrayCodec[T](implicit inner: PartialCodec[T]): PartialCodec[PureArray[T]] = new PartialCodec[PureArray[T]] {

    def decode(start: Int, source: Array[Byte]): DecodeResult[PureArray[T]] = ???
    def encode(t: PureArray[T], start: Int, destination: Array[Byte]): EncodeResult = ???
    def size(t: PureArray[T]): Int = ???

  }
}
