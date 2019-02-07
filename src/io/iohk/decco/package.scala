package io.iohk.decco


sealed trait DecodeResult[+T]
object DecodeResult {
  case object Failure extends DecodeResult[Nothing]
  case class Success[T](decoded: T, nextIndex: Int) extends DecodeResult[T]
}

sealed trait EncodeResult
object EncodeResult {
  case object EncodeSuccess extends EncodeResult
  case object NotEnoughStorage extends EncodeResult
  case object NegativeIndex extends EncodeResult
}

trait PartialCodec[T] { self =>

  def size(t: T): Int
  def typeCode: String
  def encode(t: T, start: Int, destination: Array[Byte]): EncodeResult
  def decode(start: Int, source: Array[Byte]): DecodeResult[T]

  // SOME COMPOSITION FUNCTIONS

  final def zip[U](that: PartialCodec[U]): PartialCodec[(T, U)] = new PartialCodec[(T, U)] {

    def size(t: (T, U)): Int = t match {
      case (t, u) => self.size(t) + that.size(u)
    }

    def decode(start: Int, source: Array[Byte]): io.iohk.decco.DecodeResult[(T, U)] = {
      self.decode(start, source) match {
        case DecodeResult.Failure => DecodeResult.Failure
        case DecodeResult.Success(t, nextIndex) =>
          that.decode(nextIndex, source) match {
            case DecodeResult.Failure => DecodeResult.Failure
            case DecodeResult.Success(u, resultingNextIndex) => DecodeResult.Success((t, u), resultingNextIndex)
          }
      }
    }

    def encode(tuple: (T, U), start: Int, destination: Array[Byte]): io.iohk.decco.EncodeResult = {
      val (t, u) = tuple
      val tSize = self.size(t)
      self.encode(t, start, destination) match {
        case EncodeResult.EncodeSuccess =>
          that.encode(u, start + tSize, destination)
        case error => error
      }
    }

    override val typeCode: String = this.typeCode.concat(that.typeCode)
  }

  final def map[U](uTypeCode: String, t2u: T => U, u2t: U => T): PartialCodec[U] = new PartialCodec[U] {
    def size(u: U): Int = self.size(u2t(u))
    def decode(start: Int, source: Array[Byte]): DecodeResult[U] =
      self.decode(start, source) match {
        case DecodeResult.Failure => DecodeResult.Failure
        case DecodeResult.Success(t, nextIndex) => DecodeResult.Success(t2u(t), nextIndex)
      }

    def encode(u: U, start: Int, destination: Array[Byte]): io.iohk.decco.EncodeResult =
      self.encode(u2t(u), start, destination)

    override val typeCode: String = uTypeCode
  }

}

// Make a codec responsible for framing:
// * type framing
// * size framing
// partial codec could be encoding to json, codec does not care
// it just gives an array to PartialCodec to encode into then adds its own type and size info
import scala.reflect.runtime.universe.TypeTag

final class Codec[T](partial: PartialCodec[T]) {
  def encode(t: T): Array[Byte] = {
    val size = partial.size(t)
    val r = new Array[Byte](size)
    partial.encode(t, 0, r) match {
      case EncodeResult.EncodeSuccess =>
        r
      case EncodeResult.NotEnoughStorage =>
        throw new RuntimeException(
          """|FATAL: Trying to encode an entity into an array for which there is not enough space. This should NEVER happen
             |and means that there is a bug in the partial encoder, where the partial encoder is demanding more space than
             |what it has reported it was going to be needed""".stripMargin)
      case EncodeResult.NegativeIndex =>
        throw new RuntimeException(
          """|FATAL: Trying to write into a negative index of an array. This should NEVER happen and means that there is a bug
             |in the partial encoder since this full codec is asking the partial encoder to use index 0""".stripMargin)
    }
  }

  def decode(source: Array[Byte]): Option[T] = {
    partial.decode(0, source) match {
      case DecodeResult.Failure => None
      case DecodeResult.Success(r, _) => Some(r)
    }
  }
}

object PartialCodec {
  def apply[T](implicit ev: PartialCodec[T]): PartialCodec[T] = ev

  def typeTagCode[T](implicit typeTag: TypeTag[T]): String =
    typeTag.toString()
}

object Codec {
  def apply[T](implicit ev: PartialCodec[T]): Codec[T] = codecFromPartialCodec(ev)
  implicit def codecFromPartialCodec[T](implicit partial: PartialCodec[T]): Codec[T] =
    new Codec(partial)

  // return the typecode embedded in an array.
  def peekType(source: Array[Byte]): Option[Array[Byte]] = ???

  def peekSize(source: Array[Byte]): Option[Array[Byte]] = ???
  // pipeline/meta thing
  // each object is encoded with a type code
  // the type code is written by codec, not partial codec
  // when decoding a stream:
  // we have a map of codecs
  // we use peek to extract a type code
  // that type code can be used to find the correct decoder for the current buffer position
}

/////
//
//// B is something like a Array[Byte], ByteBuffer, ByteString, etc
//sealed trait Buffer[B] {
//  type NextIndex = Int
//  def instantiate(size: Int): B
//  def writeByte(b: Byte, buffer: B): NextIndex
//  def writeByteArray(bs: Array[Byte], buffer: B): NextIndex
//  def writeFloat(f: Float, buffer: B): NextIndex
//  // ... methods for the rest of the primitive types and arrays of the primitive types
//}
//
//sealed trait DecodeResult[+T]
//object DecodeResult {
//  case object Failure extends DecodeResult[Nothing]
//  case class Success[T](decoded: T, nextIndex: Int) extends DecodeResult[T]
//}
//
//sealed trait PartialCodec[T, B] {
//  def size(t: T): Int
//  def encode(t: T, start: Int, destination: B)(implicit buf: Buffer[B]): Unit
//  def decode(start: Int, source: B)(implicit buf: Buffer[B]): DecodeResult[T]
//}
//
//final class Codec[T, B : Buffer](partial: PartialCodec[T, B]) {
//  def encode(t: T): B = {
//    val size = partial.size(t)
//    var r = implicitly[Buffer[B]].instantiate(size)
//    partial.encode(t, 0, r)
//    r
//  }
//  def decode(source: B): Option[T] = {
//    partial.deocde(0, source) match {
//      case DecodeResult.Failure => None
//      case DecodeResult.Success(r, _) => r
//    }
//  }
//}
//
//object Codec {
//  implicit def codecFromPartialCodec[T, B: Buffer](implicit partial: PartialCodec[T]): Codec[T, B] =
//    new Codec[T, B](partial)
//}
