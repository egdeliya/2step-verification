package repositories

import domain.models.User

import scala.concurrent.Future

class BannedUsersServiceDummy extends BannedUsersService {

  def checkUserIsBanned(user: User): Future[Boolean] = Future.successful(false)

  def banUser(user: User): Future[Unit] = Future.successful()

}
