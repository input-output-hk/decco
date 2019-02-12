package io.iohk.decco

import io.iohk.decco.PartialCodec.Failure
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Matchers._
import org.scalatest.prop.GeneratorDrivenPropertyChecks._
import org.scalatest.EitherValues._

object TestingHelpers {

  def partialCodecTest[T](implicit codec: PartialCodec[T], a: Arbitrary[T]): Unit = {
    encodeDecodeTest[T]
    emptyBufferTest[T]
    misalignedBufferTest[T]
  }

  private def encodeDecodeTest[T](implicit codec: PartialCodec[T], a: Arbitrary[T]): Unit = {
    forAll(arbitrary[T]) { t =>
      val arr = new Array[Byte](codec.size(t))
      codec.encode(t, 0, arr)
      val result = codec.decode(0, arr).right.value
      result.decoded shouldBe t
      result.nextIndex shouldBe arr.length
    }
  }

  private def emptyBufferTest[T](implicit codec: PartialCodec[T], a: Arbitrary[T]): Unit = {
    codec.decode(0, new Array[Byte](0)) shouldBe Left(Failure)
    codec.decode(1, new Array[Byte](1)) shouldBe Left(Failure)
  }

  private def misalignedBufferTest[T](implicit codec: PartialCodec[T], a: Arbitrary[T]): Unit = {
    forAll(arbitrary[T]) { t =>
      val arr = new Array[Byte](codec.size(t))
      codec.encode(t, 0, arr)
      codec.decode(Int.MaxValue, arr) shouldBe Left(Failure)
    }
  }
}
