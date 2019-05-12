package exceptions

case class InvalidCodeException()
  extends Exception("Invalid code!", null)
