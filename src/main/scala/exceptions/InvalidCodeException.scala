package exceptions

case class InvalidCodeException(message: String, cause: Throwable)
  extends Exception(message, cause)
