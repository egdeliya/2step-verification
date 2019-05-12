package repositories

import scala.concurrent.Future

trait SessionService {

  /**
    * Получение сессии
    */
  def getSession(phoneNum: String): Future[String]

  /**
    * Получение данных о пользователе из зашифрованной сессии
    */
  def getUserDataFromSessionToken(token: String): Future[String]

}
