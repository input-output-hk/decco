package io.iohk.decco

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class MD5Spec extends FlatSpec {

  behavior of "MD5"

  it should "have well defined equals" in {
    MD5("hello") shouldBe MD5("hello")
  }

  it should "not equal objects of other types" in {
    MD5("hello") shouldNot be (MD5("hello").hash)
  }
}
