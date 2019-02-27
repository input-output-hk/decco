package io.iohk.decco

import java.nio.ByteBuffer
import java.security.MessageDigest

import io.iohk.decco.PartialCodec.{DecodeResult, Failure}

final class MD5(val hash: Array[Byte]) {

  override def hashCode() = hash.deep.hashCode()

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: MD5 =>
        this.hash.deep == that.hash.deep
      case _ =>
        false
    }
  }
}

object MD5 {
  def apply(s: String): MD5 = new MD5(MessageDigest.getInstance("MD5").digest(s.getBytes))

  val md5Codec = new PartialCodec[MD5] {
    override def size(md5: MD5): Int = 16

    override def encode(md5: MD5, start: Int, destination: ByteBuffer): Unit = {
      md5.hash.foldLeft(0) { (acc, next) =>
        destination.put(start + acc, next)
        acc + 1
      }
    }

    override def decode(start: Int, source: ByteBuffer): Either[Failure, DecodeResult[MD5]] = {
      val dst = new Array[Byte](16)
      if (source.remaining() < 16) {
        Left(Failure)
      } else {
        dst.foldLeft(0) { (acc, _) =>
          dst(acc) = source.get(start + acc)
          acc + 1
        }
        Right(DecodeResult(new MD5(dst), start + 16))
      }
    }
  }
}
