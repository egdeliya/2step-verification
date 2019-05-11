package exceptions

case class InternalServerErrorException()
  extends Exception("Some error occurred :( ", null)
