package io.iohk.decco.instances

import java.nio.ByteBuffer

import io.iohk.decco.PartialCodec

trait NativeInstances {

  implicit val BytePartialCodec: PartialCodec[Byte] = new NativePartialCodec[Byte](size = 1) {
    def encode(b: Byte, start: Int, destination: ByteBuffer): Unit =
      destination.put(start, b)

    def doDecode(start: Int, source: ByteBuffer): Byte =
      source.get(start)
  }

  implicit val ShortPartialCodec: PartialCodec[Short] = new NativePartialCodec[Short](size = 2) {
    def encode(s: Short, start: Int, destination: ByteBuffer): Unit =
      destination.putShort(start, s)

    def doDecode(start: Int, source: ByteBuffer): Short =
      source.getShort(start)
  }

  implicit val IntPartialCodec: PartialCodec[Int] = new NativePartialCodec[Int](size = 4) {
    def encode(i: Int, start: Int, destination: ByteBuffer): Unit =
      destination.putInt(start, i)

    def doDecode(start: Int, source: ByteBuffer): Int =
      source.getInt(start)
  }

  implicit val LongPartialCodec: PartialCodec[Long] = new NativePartialCodec[Long](size = 8) {
    override def encode(l: Long, start: Int, destination: ByteBuffer): Unit =
      destination.putLong(start, l)

    override def doDecode(start: Int, source: ByteBuffer): Long =
      source.getLong(start)
  }

  implicit val BooleanPartialCodec: PartialCodec[Boolean] = new NativePartialCodec[Boolean](size = 1) {
    def encode(b: Boolean, start: Int, destination: ByteBuffer): Unit =
      destination.put(start, if (b) 1 else 0)

    def doDecode(start: Int, source: ByteBuffer): Boolean =
      source.get(start) == 1
  }

  implicit val CharPartialCodec: PartialCodec[Char] = new NativePartialCodec[Char](size = 2) {
    override def encode(c: Char, start: Int, destination: ByteBuffer): Unit =
      destination.putChar(start, c)

    override def doDecode(start: Int, source: ByteBuffer): Char =
      source.getChar(start)
  }

  implicit val FloatPartialCodec: PartialCodec[Float] = new NativePartialCodec[Float](size = 4) {
    override def encode(f: Float, start: Int, destination: ByteBuffer): Unit =
      destination.putFloat(start, f)

    override def doDecode(start: Int, source: ByteBuffer): Float =
      source.getFloat(start)
  }

  implicit val DoublePartialCodec: PartialCodec[Double] = new NativePartialCodec[Double](8) {
    override def encode(d: Double, start: Int, destination: ByteBuffer): Unit =
      destination.putDouble(start, d)

    override def doDecode(start: Int, source: ByteBuffer): Double =
      source.getDouble(start)
  }
}

object NativeInstances extends NativeInstances
