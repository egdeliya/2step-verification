package exceptions

case class WeakPasswordException(message: String, cause: Throwable)
  extends Exception(message, cause)
