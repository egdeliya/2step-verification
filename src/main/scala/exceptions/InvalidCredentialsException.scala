package exceptions

case class InvalidCredentialsException()
  extends Exception("Invalid credentials", null)
