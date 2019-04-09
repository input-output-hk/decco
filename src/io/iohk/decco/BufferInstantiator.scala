package io.iohk.decco

import java.nio.ByteBuffer

import scala.annotation.implicitNotFound

@implicitNotFound("""Cannot find an implicit BufferInstantiator. You might pass
an (implicit ec: BufferInstantiator) parameter to your method.

Otherwise, you can import one of the two provided BufferInstantiotor with the
following:

import io.iohk.decco.BufferInstantiator.global.HeapByteBuffer

or the following:

import io.iohk.decco.BufferInstantiator.global.DirectByteBuffer
""")
trait BufferInstantiator[B] { self =>
  def instantiateByteBuffer(size: Int): ByteBuffer
  def asB(byteBuffer: ByteBuffer): B
  def asByteBuffer(b: B): ByteBuffer

  def map[C](toC: B => C, fromC: C => B): BufferInstantiator[C] =
    new BufferInstantiator[C] {
      def instantiateByteBuffer(size: Int): ByteBuffer = self.instantiateByteBuffer(size)
      def asB(byteBuffer: ByteBuffer): C = toC(self.asB(byteBuffer))
      def asByteBuffer(c: C): ByteBuffer = self.asByteBuffer(fromC(c))
    }
}

object BufferInstantiator {

  object global {

    /*
     * From ByteBuffer docs:
     * A direct byte buffer may be created by invoking the allocateDirect factory method of this class.
     * The buffers returned by this method typically have somewhat higher allocation and deallocation costs than non-direct buffers.
     * The contents of direct buffers may reside outside of the normal garbage-collected heap,
     * and so their impact upon the memory footprint of an application might not be obvious.
     * It is therefore recommended that direct buffers be allocated primarily for large, long-lived buffers
     * that are subject to the underlying system's native I/O operations.
     * In general it is best to allocate direct buffers only when they yield a measureable gain in program performance.
     */
    implicit object HeapByteBuffer extends BufferInstantiator[ByteBuffer] {
      def instantiateByteBuffer(size: Int): ByteBuffer =
        ByteBuffer.allocate(size)
      def asB(byteBuffer: ByteBuffer): ByteBuffer = byteBuffer
      def asByteBuffer(b: ByteBuffer): ByteBuffer = b
    }

    implicit object DirectByteBuffer extends BufferInstantiator[ByteBuffer] {
      def instantiateByteBuffer(size: Int): ByteBuffer =
        ByteBuffer.allocateDirect(size)
      def asB(byteBuffer: ByteBuffer): ByteBuffer = byteBuffer
      def asByteBuffer(b: ByteBuffer): ByteBuffer = b
    }

  }

}
