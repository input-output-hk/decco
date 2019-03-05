package io.iohk.decco

import java.nio.ByteBuffer

import io.iohk.decco.PartialCodec.Failure
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Matchers._
import org.scalatest.prop.GeneratorDrivenPropertyChecks._
import org.scalatest.EitherValues._

import scala.util.Random

object TestingHelpers {

  def partialCodecTest[T](implicit codec: PartialCodec[T], a: Arbitrary[T]): Unit = {
    encodeDecodeTest[T]
    emptyBufferTest[T]
    misalignedBufferTest[T]
  }

  private def encodeDecodeTest[T](implicit codec: PartialCodec[T], a: Arbitrary[T]): Unit = {
    forAll(arbitrary[T]) { t =>
      val buff = ByteBuffer.allocate(codec.size(t))
      codec.encode(t, 0, buff)
      val result = codec.decode(0, buff).right.value
      result.decoded shouldBe t
      result.nextIndex shouldBe buff.capacity
    }
  }

  private def emptyBufferTest[T](implicit codec: PartialCodec[T], a: Arbitrary[T]): Unit = {
    codec.decode(0, ByteBuffer.allocate(0)) shouldBe Left(Failure)
    codec.decode(1, ByteBuffer.allocate(1)) shouldBe Left(Failure)
  }

  private def misalignedBufferTest[T](implicit codec: PartialCodec[T], a: Arbitrary[T]): Unit = {
    forAll(arbitrary[T]) { t =>
      val buff = ByteBuffer.allocate(codec.size(t))
      codec.encode(t, 0, buff)
      codec.decode(Int.MaxValue, buff) shouldBe Left(Failure)
    }
  }

  def largeArrayTest(buffSz: Int)(implicit codec: PartialCodec[Array[Byte]]): Unit = {
    val data = randomBytes(buffSz)
    val buff = ByteBuffer.allocate(buffSz + 4)
    codec.encode(data, 0, buff)
    val result = codec.decode(0, buff).right.value
    result.decoded shouldBe data
    result.nextIndex shouldBe buff.capacity
  }

  private def randomBytes(n: Int): Array[Byte] = {
    val a = new Array[Byte](n)
    Random.nextBytes(a)
    a
  }
}
