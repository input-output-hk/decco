package io.iohk.decco

sealed trait DecodeFailure

object DecodeFailure {
  case object HeaderWrongFormat extends DecodeFailure

  case object BodyTooShort extends DecodeFailure

  case object BodyWrongType extends DecodeFailure

  case object BodyWrongFormat extends DecodeFailure
}