package io.iohk.decco
package test.utils
import java.nio.ByteBuffer

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalactic.Equivalence
import org.scalatest.Inside.inside
import org.scalatest.Matchers.equal
import org.scalatest.prop.GeneratorDrivenPropertyChecks._
import scala.util.Random
import org.scalatest.Matchers._
import org.scalatest.EitherValues._
import auto._

trait CodecTestingHelpers {

  case class UnexpectedThing()
  object UnexpectedThing {
    implicit val UnexpectedThingArbitrary: Arbitrary[UnexpectedThing] =
      Arbitrary(arbitrary[Unit].map(_ => UnexpectedThing()))
  }

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

  def mistypeTest[T, U](
      implicit codecU: Codec[U],
      codecT: Codec[T],
      a: Arbitrary[T]
  ): Unit = {

    forAll(arbitrary[T]) { t =>
      val buff: ByteBuffer = codecT.encode(t)
      val dec = codecU.decode(buff)
      dec.isLeft shouldBe true
    }
  }

  def variableLengthTest[T](
      implicit codec: Codec[T],
      a: Arbitrary[T]
  ): Unit = {
    forAll(arbitrary[T]) { t =>
      // create a buffer with one half full of real data
      // and the second half full of rubbish.
      // codecs should not be fooled by this.
      val b: ByteBuffer = codec.encode(t)
      val newB = ByteBuffer
        .allocate(b.capacity() * 2)
        .put(b)
        .put(randomBytes(b.capacity()))
      (newB: java.nio.Buffer).flip()

      inside(codec.decode(newB)) {
        case Right(tt) => tt shouldBe t
      }
    }
  }

  def unfulBufferTest[T](implicit codec: Codec[T]): Unit = {

    codec.decode(ByteBuffer.allocate(0)).isLeft shouldBe true
    codec.decode(ByteBuffer.allocate(5)).isLeft shouldBe true

    val b = ByteBuffer.allocate(5)
    b.put(-1.toByte)
    b.put(-12.toByte)
    b.put(-1.toByte)
    b.put(-128.toByte)
    b.put(-118.toByte)
    codec.decode(b).isLeft shouldBe true
  }

  def testFull[T: Codec: Arbitrary]: Unit = {
    encodeDecodeTest[T]
    variableLengthTest[T]
    unfulBufferTest[T]
    mistypeTest[T, UnexpectedThing]
    mistypeTest[UnexpectedThing, T]
  }
  def testWhenNotEncodingType[T: Codec: Arbitrary]: Unit = {
    encodeDecodeTest[T]
    variableLengthTest[T]
    unfulBufferTest[T]
  }

  def randomBytes(n: Int): Array[Byte] = {
    val a = new Array[Byte](n)
    Random.nextBytes(a)
    a
  }

  def concatenate(buffs: Seq[ByteBuffer]): ByteBuffer = {
    val allocSize =
      buffs.foldLeft(0)((acc, nextBuff) => acc + nextBuff.capacity())

    val b0 = ByteBuffer.allocate(allocSize)

    (buffs.foldLeft(b0)((accBuff, nextBuff) => accBuff.put(nextBuff)): java.nio.Buffer)
      .flip()
      .asInstanceOf[ByteBuffer]
  }
}
