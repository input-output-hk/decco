package io.iohk.decco
package auto.instances

import java.net.{InetAddress, InetSocketAddress}
import java.time._
import java.util.UUID

trait OtherInstances {
  implicit def BigDecimalInstance(implicit stringPf: Codec[String]): Codec[BigDecimal] =
    stringPf.map(s => BigDecimal(s), bd => bd.toString())

  implicit def UUIDInstance(implicit stringPf: Codec[String]): Codec[UUID] =
    stringPf.map(s => UUID.fromString(s), uuid => uuid.toString)

  implicit def InstantInstance(implicit pf: Codec[(Long, Long)]): Codec[Instant] =
    pf.map(
      ll => Instant.ofEpochSecond(ll._1, ll._2),
      instant => (instant.getEpochSecond, instant.getNano)
    )

  implicit def LocalDateInstance(implicit pf: Codec[Long]): Codec[LocalDate] =
    pf.map(l => LocalDate.ofEpochDay(l), localDate => localDate.toEpochDay)

  implicit def LocalTimeInstance(implicit pf: Codec[Long]): Codec[LocalTime] =
    pf.map(l => LocalTime.ofNanoOfDay(l), localTime => localTime.toNanoOfDay)

  implicit def LocalDateTimeInstance(implicit pf: Codec[(Long, Long)]): Codec[LocalDateTime] =
    pf.map(
      ll => LocalDateTime.of(LocalDate.ofEpochDay(ll._1), LocalTime.ofNanoOfDay(ll._2)),
      localDateTime => (localDateTime.toLocalDate.toEpochDay, localDateTime.toLocalTime.toNanoOfDay)
    )

  implicit def InetAddressInstance(implicit pf: Codec[Array[Byte]]): Codec[InetAddress] =
    pf.map(arr => InetAddress.getByAddress(arr), inetAddress => inetAddress.getAddress)

  implicit def InetSocketAddressInstance(implicit pf: Codec[(String, Int)]): Codec[InetSocketAddress] =
    pf.map(
      { case (host: String, port: Int) => new InetSocketAddress(host, port) },
      inetSocketAddress => (inetSocketAddress.getHostName, inetSocketAddress.getPort)
    )

}

object OtherInstances extends OtherInstances
