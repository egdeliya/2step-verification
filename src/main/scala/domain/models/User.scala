package domain.models

// TODO make user without password check
case class User(phone: String,
                pass: String) {

  private val phoneNumber = PhoneNumber(phone)
  private val password = Password(pass)

  def getPhone: String = phoneNumber.getValue
  def getPass: String = password.getValue
}
