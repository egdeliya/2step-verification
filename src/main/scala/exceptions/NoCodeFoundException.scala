package exceptions

case class NoCodeFoundException()
  extends Exception("No code was found for user!", null)
