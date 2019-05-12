package repositories

import com.typesafe.scalalogging.StrictLogging
import Cassandra.CassandraDb
import domain.models.User
import exceptions.{InvalidCredentialsException, UserAlreadyRegisteredException, UserBannedException, UserNotRegisteredException}
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthServiceCassandra(private val database: CassandraDb,
                           private val bannedUsersService: BannedUsersService,
                           private val smsService: SmsService)
  extends AuthService
  with StrictLogging {

  def login(user: User): Future[String] = {
    checkUserNotBannedAndRegistered(user)
      .flatMap {
        case Some(foundUser) if BCrypt.checkpw(user.getPass, foundUser.getPass) =>
          smsService.sendCode(user)

        case Some(_) =>
          logger.debug(s"User ${user.getPhone} entered wrong password")
          throw InvalidCredentialsException()

        case _ =>
          logger.debug(s"Not registered user ${user.getPhone} tries to login")
          throw UserNotRegisteredException(user.getPhone)
      }
  }

  def register(user: User): Future[Unit] = {
    checkUserNotBannedAndRegistered(user)
      .flatMap {
        case Some(_) =>
          logger.debug(s"User ${user.getPhone} already registered")
          throw UserAlreadyRegisteredException(s"User ${user.getPhone} already registered", null)

        case _ =>
          database.registerUser(user.getPhone, user.getPass)
      }
  }

  private def checkUserNotBannedAndRegistered(user: User): Future[Option[User]] = {
    bannedUsersService
      .checkUserIsBanned(user)
      .flatMap {
        case isBanned if isBanned =>
          logger.warn(s"Banned user ${user.getPhone} tries to enter service")
          throw UserBannedException("User is banned", null)

        case _ =>
          database.checkUserRegistered(user.getPhone)
      }
  }
}
