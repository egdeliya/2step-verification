package exceptions

case class UserAlreadyRegisteredException(message: String, cause: Throwable)
  extends Exception(message, cause)
