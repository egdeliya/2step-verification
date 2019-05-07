package exceptions

case class UserBannedException(message: String, cause: Throwable)
  extends Exception(message, cause)
