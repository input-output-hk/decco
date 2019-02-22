package io.iohk.decco.instances

import java.net.{InetAddress, InetSocketAddress}
import java.time._
import java.util.UUID

import io.iohk.decco.{PartialCodec, TypeCode}

trait OtherInstances {
  implicit def BigDecimalInstance(implicit stringPf: PartialCodec[String]): PartialCodec[BigDecimal] =
    stringPf.map(TypeCode[BigDecimal], s => BigDecimal(s), bd => bd.toString())

  implicit def UUIDInstance(implicit stringPf: PartialCodec[String]): PartialCodec[UUID] =
    stringPf.map(TypeCode[UUID], s => UUID.fromString(s), uuid => uuid.toString)

  implicit def InstantInstance(implicit pf: PartialCodec[(Long, Long)]): PartialCodec[Instant] =
    pf.map(
      TypeCode[Instant],
      ll => Instant.ofEpochSecond(ll._1, ll._2),
      instant => (instant.getEpochSecond, instant.getNano)
    )

  implicit def LocalDateInstance(implicit pf: PartialCodec[Long]): PartialCodec[LocalDate] =
    pf.map(TypeCode[LocalDate], l => LocalDate.ofEpochDay(l), localDate => localDate.toEpochDay)

  implicit def LocalTimeInstance(implicit pf: PartialCodec[Long]): PartialCodec[LocalTime] =
    pf.map(TypeCode[LocalTime], l => LocalTime.ofNanoOfDay(l), localTime => localTime.toNanoOfDay)

  implicit def LocalDateTimeInstance(implicit pf: PartialCodec[(Long, Long)]): PartialCodec[LocalDateTime] =
    pf.map(
      TypeCode[LocalDateTime],
      ll => LocalDateTime.of(LocalDate.ofEpochDay(ll._1), LocalTime.ofNanoOfDay(ll._2)),
      localDateTime => (localDateTime.toLocalDate.toEpochDay, localDateTime.toLocalTime.toNanoOfDay)
    )

  implicit def InetAddressInstance(implicit pf: PartialCodec[Array[Byte]]): PartialCodec[InetAddress] =
    pf.map(TypeCode[InetAddress], arr => InetAddress.getByAddress(arr), inetAddress => inetAddress.getAddress)

  implicit def InetSocketAddressInstance(implicit pf: PartialCodec[(String, Int)]): PartialCodec[InetSocketAddress] =
    pf.map(
      TypeCode[InetSocketAddress],
      { case (host: String, port: Int) => new InetSocketAddress(host, port) },
      inetSocketAddress => (inetSocketAddress.getHostName, inetSocketAddress.getPort)
    )
}

object OtherInstances extends OtherInstances
