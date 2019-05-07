package repositories

import com.typesafe.scalalogging.StrictLogging

import Cassandra.CassandraDb
import domain.models.User
import exceptions.{UserAlreadyRegisteredException, UserBannedException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthServiceCassandra(private val database: CassandraDb,
                           private val bannedUsersService: BannedUsersService) extends AuthService
  with StrictLogging {

  def login(user: User): Future[String] = {
    Future.successful("")
  }

  def register(user: User): Future[Unit] = {
    bannedUsersService
      .checkUserIsBanned(user)
      .flatMap {
        case isBanned if isBanned =>
          logger.warn(s"Banned user ${user.getPhone} tries to enter service")
          throw UserBannedException("User is banned", null)

        case _ =>
          checkUserRegistered(user)
      }.flatMap {
        case isRegistered if isRegistered =>
          logger.warn(s"User ${user.getPhone} already registered")
          throw UserAlreadyRegisteredException(s"User ${user.getPhone} already registered", null)

        case _ =>
          database.registerUser(user.getPhone, user.getPass)
    }
  }

  def checkUserRegistered(user: User): Future[Boolean] = {
    database.checkUserRegistered(user.getPhone)
  }
}
