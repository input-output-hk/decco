package io.iohk.decco

sealed trait DecodeFailure

object DecodeFailure {
  case object HeaderWrongFormat extends DecodeFailure

  case class BodyTooShort(expectedBytes: Int, encounteredBytes: Int) extends DecodeFailure

  case class BodyWrongType(expectedType: String, encounteredType: String) extends DecodeFailure

  case object BodyWrongFormat extends DecodeFailure
}
