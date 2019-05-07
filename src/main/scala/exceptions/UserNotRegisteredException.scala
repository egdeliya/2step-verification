package exceptions

case class UserNotRegisteredException(message: String, cause: Throwable)
  extends Exception(message, cause)
