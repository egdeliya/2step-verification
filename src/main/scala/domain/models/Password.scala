package domain.models

import exceptions.WeakPasswordException
import org.mindrot.jbcrypt.BCrypt

import scala.util.matching.Regex

object Password {
  private val upperCaseCheck = "[A-Z]".r
  private val lowerCaseCheck = "[a-z]".r
  private val numCheck = "[0-9]".r
  private val safeEnoughLen = 1

  private def lenCheck(value: String) = value.length > safeEnoughLen
  private def regexMatch(regex: Regex, value: String) = regex.findFirstMatchIn(value).isDefined

  def apply(value: String): Password = {
    if (!regexMatch(upperCaseCheck, value)) throw WeakPasswordException("Password should contain at least one upper case symbol", null)
    else if (!regexMatch(lowerCaseCheck, value)) throw WeakPasswordException("Password should contain at least one lower case symbol", null)
    else if (!regexMatch(numCheck, value)) throw WeakPasswordException("Password should contain at least one number", null)
    else if (!lenCheck(value)) throw WeakPasswordException(s"Password length should be at least $safeEnoughLen", null)
    else new Password(value)
  }
}
class Password private (private val value: String) {
  val getValue: String = value
}
