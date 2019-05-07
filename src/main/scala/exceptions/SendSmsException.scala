package exceptions

case class SendSmsException(message: String, cause: Throwable)
  extends Exception(message, cause)
