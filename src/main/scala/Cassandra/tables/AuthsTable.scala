package Cassandra.tables

import com.outworkers.phantom.dsl._
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.Future

abstract class AuthsTable extends Table[AuthsTable, String] {

  override def tableName: String = "auths"

  object phoneNumber extends StringColumn with PartitionKey
  object password extends StringColumn

  def checkUserRegistered(phoneNumber: String): Future[Boolean] = {
    select
      .where(_.phoneNumber eqs phoneNumber)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .one()
      .collect {
        case Some(_) => true
        case None => false
      }
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
