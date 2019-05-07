package exceptions

case class InvalidPhoneNumberException(message: String, cause: Throwable)
  extends Exception(message, cause)
