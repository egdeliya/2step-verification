package repositories

import domain.models.User

import scala.concurrent.Future

trait AuthService {
  /**
    * Вход пользователя по номеру телефона и паролю
    * @return Future[code: String], code - sms код, отправленный пользователю
    */
  def login(user: User): Future[String]

  /**
    * Регистрация пользователя
    * @return Future.successful() - в случае удачи, Future.failed() иначе
    */
  def register(user: User): Future[Unit]

  /**
    * Верификация пользователя
    * @return Future.successful() - в случае удачи, Future.failed() иначе
    */
  def verifyCode(phoneNum: String, code: String): Future[Unit]
}
