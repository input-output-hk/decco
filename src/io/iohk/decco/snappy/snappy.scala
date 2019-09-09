package io.iohk.decco.snappy

import java.nio.ByteBuffer

import io.iohk.decco.Codec.Failure
import io.iohk.decco.auto.instances.TraversableCodecContract
import io.iohk.decco.{BufferInstantiator, Codec, CodecContract}
import org.xerial.snappy.Snappy

class SnappyCodec[T](implicit tCodec: CodecContract[T], byteCodec: CodecContract[Byte], intCodec: CodecContract[Int])
    extends Codec[T] {

  protected val arrayCodec = new TraversableCodecContract[Byte, Array[Byte]]()

  override def encode[B](t: T)(implicit bi: BufferInstantiator[B]): B = {
    val underlyingBytes = bi.asByteBuffer(tCodec.encode(t))

    arrayCodec.encode(Snappy.compress(underlyingBytes.array()))
  }

  def decode[B](bytes: Array[Byte])(implicit bi: BufferInstantiator[B]): Either[Failure, T] = {
    tCodec.decode(bi.asB(ByteBuffer.wrap(Snappy.uncompress(bytes))))
  }

  // FIXME: Issue #2: we are unable to tell where encoded message ends in the bytestream,
  //                  so we have to prepend it with its size (using Array[Byte] codec)

  override def decode[B](start: Int, source: B)(implicit bi: BufferInstantiator[B]): Either[Failure, T] = {
    arrayCodec.decode(start, source).flatMap(decode[B](_))
  }

  override def decode[B](source: B)(implicit bi: BufferInstantiator[B]): Either[Failure, T] = {
    arrayCodec.decode(source).flatMap(decode[B](_))
  }
}

package object auto {
  implicit def snappy[T](
      implicit tCodec: CodecContract[T],
      byteCodec: CodecContract[Byte],
      intCodec: CodecContract[Int]
  ): Codec[T] =
    new SnappyCodec[T]()
}
