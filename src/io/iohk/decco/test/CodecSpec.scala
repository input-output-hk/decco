package io.iohk.decco
package test

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.UUID

import io.iohk.decco.BufferInstantiator.global.HeapByteBuffer
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import io.iohk.decco.auto._
import org.scalatest.EitherValues._
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary._
import io.iohk.decco.test.utils.CodecTestingHelpers

class CodecSpec extends FlatSpec with CodecTestingHelpers {

  behavior of "Codecs"

  case class A(s: String, i: Int, l: List[String], u: UUID, f: Float, ia: InetSocketAddress)

  case class B(s: String, i: Int, l: List[String], u: UUID, f: Float)

  case class Wrap[T](t: T)

  sealed trait Base[T]

  case class S1[T](t: T) extends Base[T]

  case class S2[T]() extends Base[T]

  implicit val arbitraryA: Arbitrary[A] = Arbitrary(for {
    s <- arbitrary[String]
    i <- arbitrary[Int]
    l <- arbitrary[List[String]]
    u <- arbitrary[UUID]
    f <- arbitrary[Float]
    a <- arbitrary[String]
    p <- Gen.choose[Int](0, 65535)
  } yield A(s, i, l, u, f, new InetSocketAddress(a, p)))

  they should "work for fixed width types" in {
    testCodec[Int]
  }

  they should "work for variable width types" in {
    testCodec[String]
  }

  they should "work for collection types" in {
    testCodec[List[String]]
  }

  they should "work for user types" in {
    testCodec[A]
  }

  they should "compose from generic types" in {
    def wrappingTest[T: CodecContract: Arbitrary] = {

      implicit val arbitraryWrap: Arbitrary[Wrap[T]] = Arbitrary(
        for {
          t <- arbitrary[T]
        } yield Wrap(t)
      )

      testCodec[Wrap[T]]
    }

    wrappingTest[A]
  }

  they should "support the recovery of backing arrays with heap codec" in {
    val bytes = Codec[String].encode("string")

    val backingArray: Array[Byte] = bytes.array()

    Codec[String].decode(ByteBuffer.wrap(backingArray)).right.value shouldBe "string"
  }

}
