package repositories

import domain.models.User

import scala.concurrent.Future

trait AuthService {
  /**
    * Вход пользователя по номеру телефона и паролю
    * @return Future от authLevel - ("registered", "loggedIn", "anonim")
    */
  def login(user: User): Future[String]

  /**
    * Регистрация пользователя
    * @return Future.successful() - в случае удачи, Future.failed() иначе
    */
  def register(user: User): Future[Unit]

  /**
    * Проверка, что пользователь уже зарегистрирован
    * @return Future[result : Boolean] - true - зарегистрирован, false - нет
    */
  def checkUserRegistered(user: User): Future[Boolean]
}
