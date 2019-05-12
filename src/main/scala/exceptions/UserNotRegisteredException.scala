package exceptions

case class UserNotRegisteredException(phone: String)
  extends Exception(s"User $phone not registered yet", null)
