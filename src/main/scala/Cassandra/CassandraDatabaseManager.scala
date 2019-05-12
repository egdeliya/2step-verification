package Cassandra

import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.database.Database
import Cassandra.tables._
import domain.models.User

import scala.concurrent.duration._
import scala.concurrent.Future

class CassandraDb(override val connector: CassandraConnection) extends Database[CassandraDb](connector) {
  object Auths extends AuthsTable with connector.Connector
  object SmsCodes extends VerificationCodesTable with connector.Connector

  def createTablesIfNotExists(): Unit = {
    this.create(Duration.Inf)
  }

  def cleanup(): Unit = {
    this.cleanup()
  }

  def registerUser(phoneNumber: String, password: String): Future[Unit] = {
    Auths.registerUser(phoneNumber, password)
  }

  def checkUserRegistered(phoneNumber: String): Future[Option[User]] = {
    Auths.checkUserRegistered(phoneNumber)
  }

  def storeSmsCode(phoneNumber: String, code: String): Future[String] = {
    SmsCodes.storeSmsCode(phoneNumber, code)
  }

  def getSmsCode(phoneNumber: String): Future[Option[(String, String)]] = {
    SmsCodes.getSmsCode(phoneNumber)
  }

}