package io.iohk.decco
package test.utils
import java.nio.ByteBuffer

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalactic.Equivalence
import org.scalatest.Matchers.equal
import org.scalatest.prop.GeneratorDrivenPropertyChecks._
import scala.util.Random
import org.scalatest.Matchers._
import org.scalatest.EitherValues._

import io.iohk.decco.BufferInstantiator.global.HeapByteBuffer

trait CodecTestingHelpers {

  def encodeDecodeTest[T](
      implicit codec: Codec[T],
      a: Arbitrary[T],
      eq: Equivalence[T]
  ): Unit = {
    forAll(arbitrary[T]) { t =>
      val e = codec.encode(t)
      codec.decode(e).right.value should equal(t)
    }
  }

  def testCodec[A, B <: A](
      implicit codecA: Codec[A],
      codecB: Codec[B],
      aa: Arbitrary[B],
      eq: Equivalence[A]
  ): Unit = {
    forAll(arbitrary[B]) { t =>
      codecA.decode(codecA.encode(t)).right.value should equal(t)
      codecB.decode(codecB.encode(t)).right.value should equal(t)
      codecB.decode(codecA.encode(t)).right.value should equal(t)
      codecA.decode(codecB.encode(t)) should equal(Right(t))
    }
  }

  def variableLengthTest[T](
      implicit codec: Codec[T],
      a: Arbitrary[T],
      eq: Equivalence[T]
  ): Unit = {
    forAll(arbitrary[T]) { t =>
      // create a buffer with one half full of real data
      // and the second half full of rubbish.
      // codecs should not be fooled by this.
      val b = codec.encode(t).toArray
      val newB = ByteBuffer
        .allocate(b.length * 2)
        .put(b)
        .put(randomBytes(b.length))
      (newB: java.nio.Buffer).position(0)

      codec.decode(newB).right.value shouldBe t
    }
  }

  def testCodec[T: Codec: Arbitrary]: Unit = {
    encodeDecodeTest[T]
    variableLengthTest[T]
  }

  private def randomBytes(n: Int): Array[Byte] = {
    val a = new Array[Byte](n)
    Random.nextBytes(a)
    a
  }

  private def concatenate(buffs: Seq[ByteBuffer]): ByteBuffer = {
    val allocSize =
      buffs.foldLeft(0)((acc, nextBuff) => acc + nextBuff.capacity())

    val b0 = ByteBuffer.allocate(allocSize)

    (buffs.foldLeft(b0)((accBuff, nextBuff) => accBuff.put(nextBuff)): java.nio.Buffer)
      .flip()
      .asInstanceOf[ByteBuffer]
  }

  implicit private class ByteBufferConversionOps(val byteBuffer: ByteBuffer) {
    def toArray: Array[Byte] = {
      if (byteBuffer.hasArray)
        byteBuffer.array
      else {
        (byteBuffer: java.nio.Buffer).position(0)
        val arr = new Array[Byte](byteBuffer.remaining())
        byteBuffer.get(arr)
        arr
      }
    }
  }

}

object CodecTestingHelpers extends CodecTestingHelpers
