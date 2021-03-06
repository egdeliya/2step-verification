package Cassandra.tables

import com.outworkers.phantom.dsl._
import domain.models.User
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.Future

abstract class AuthsTable extends Table[AuthsTable, User] {

  override def tableName: String = "auths"

  object phoneNumber extends StringColumn with PartitionKey
  object password extends StringColumn

  def checkUserRegistered(phoneNumber: String): Future[Option[User]] = {
    logger.debug("Check User Registered in Auths table")
    select
      .where(_.phoneNumber eqs phoneNumber)
      .one()
  }

  def registerUser(phoneNumber: String, password: String): Future[Unit] = {
    insert
      .value(_.phoneNumber, phoneNumber)
      .value(_.password, BCrypt.hashpw(password, BCrypt.gensalt()))
      .ifNotExists()
      .future()
      .map{ _ => Unit}
  }
}
