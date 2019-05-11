package repositories

import domain.models.User

import scala.concurrent.Future

trait BannedUsersService {
  /**
    * Проверка, что номер телефона не забанен
    * @return Future[result : Boolean] - true - забанен, false - нет
    */
  def checkUserIsBanned(user: User): Future[Boolean]

  /**
    * Бан пользователя
    */
  def banUser(user: User): Future[Unit]
}
