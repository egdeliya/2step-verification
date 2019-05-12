package repositories

import domain.models.User

import scala.concurrent.Future

trait SmsService {
  /**
    * Сообщение пользователю по номеру телефона
    * @return Future[code: String], code - sms код, отправленный пользователю
    */
  def sendCode(user: User): Future[String]
}
