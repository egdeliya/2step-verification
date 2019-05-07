package domain.models

import exceptions.InvalidPhoneNumberException

import scala.util.matching.Regex

object PhoneNumber {
  private val phoneNumRegex = "79\\d{9}".r

  private def regexMatch(regex: Regex, value: String) = regex.findFirstMatchIn(value).isDefined

  def apply(value: String): PhoneNumber = {
    if (regexMatch(phoneNumRegex, value)) {
      new PhoneNumber(value)
    } else {
      throw InvalidPhoneNumberException(s"Phone number should match '79*********' pattern", null)
    }
  }
}
class PhoneNumber private (private val value: String) {
  val getValue: String = value
}

