package io.iohk.decco
package test.instances

import java.net.{InetAddress, InetSocketAddress}
import java.nio.ByteBuffer
import java.time._
import java.util.UUID

import io.iohk.decco.test.utils.CodecTestingHelpers._
import org.scalatest.FlatSpec
import io.iohk.decco.auto._
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._

class OtherInstancesSpec extends FlatSpec {

  implicit val arbitraryByteBuffer: Arbitrary[ByteBuffer] = Arbitrary(
    arbitrary[Array[Byte]].map(arr => ByteBuffer.wrap(arr))
  )

  //Borrowed from here:
  //https://github.com/rallyhealth/scalacheck-ops/blob/master/core/src/main/scala/org/scalacheck/ops/time/ImplicitJavaTimeGenerators.scala
  implicit val arbitraryInstant: Arbitrary[Instant] = {
    Arbitrary {
      for {
        millis <- chooseNum(
          Instant.MIN.getEpochSecond,
          Instant.MAX.getEpochSecond
        )
        nanos <- chooseNum(Instant.MIN.getNano, Instant.MAX.getNano)
      } yield {
        Instant.ofEpochMilli(millis).plusNanos(nanos)
      }
    }
  }

  implicit val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary(
    arbitraryInstant.arbitrary.map(instant => instant.atZone(ZoneId.systemDefault()).toLocalDate)
  )

  implicit val arbitraryLocalTime: Arbitrary[LocalTime] = Arbitrary(
    arbitraryInstant.arbitrary.map(instant => instant.atZone(ZoneId.systemDefault()).toLocalTime)
  )

  implicit val arbitraryLocalDateTime: Arbitrary[LocalDateTime] = Arbitrary(
    arbitraryInstant.arbitrary.map(instant => instant.atZone(ZoneId.systemDefault()).toLocalDateTime)
  )

  implicit val arbitraryInetAddress: Arbitrary[InetAddress] = Arbitrary(
    for {
      i <- Gen.choose(0, Int.MaxValue)
      arr = ByteBuffer.allocate(4).putInt(i).array()
    } yield InetAddress.getByAddress(arr)
  )

  implicit val arbitraryInetSocketAddress: Arbitrary[InetSocketAddress] = Arbitrary(
    for {
      s <- arbitrary[String]
      i <- Gen.choose[Int](0, 65535)
    } yield new InetSocketAddress(s, i)
  )

  behavior of "Other instances"

  they should "work" in {
    testCodec[BigDecimal]
    testCodec[UUID]
    testCodec[Instant]
    testCodec[LocalDate]
    testCodec[LocalTime]
    testCodec[LocalDateTime]
    testCodec[InetAddress]
    testCodec[InetSocketAddress]
  }
}
