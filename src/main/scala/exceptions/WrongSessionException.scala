package exceptions

case class WrongSessionException()
  extends Exception("Wrong session!", null)
