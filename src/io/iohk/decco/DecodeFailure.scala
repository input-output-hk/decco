package io.iohk.decco

sealed trait DecodeFailure

object DecodeFailure {
  case object HeaderWrongFormat extends DecodeFailure

  case class BodyTooShort(expectedBytes: Int, encounteredBytes: Int) extends DecodeFailure

  case class BodyWrongType(expectedType: MD5, encounteredType: MD5) extends DecodeFailure

  case object BodyWrongFormat extends DecodeFailure

//  case object NoMatchingObjectInBuffer
}
