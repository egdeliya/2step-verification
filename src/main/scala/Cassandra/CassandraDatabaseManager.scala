package Cassandra

import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.database.Database
import Cassandra.tables._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class CassandraDb(override val connector: CassandraConnection) extends Database[CassandraDb](connector) {
  object Auths extends AuthsTable with connector.Connector

  def createTablesIfNotExists(): Unit = {
    this.create(Duration.Inf)
  }

  def cleanup(): Unit = {
    this.cleanup()
  }

  def registerUser(phoneNumber: String, password: String): Future[Unit] = {
    Auths.registerUser(phoneNumber, password)
  }

  def checkUserRegistered(phoneNumber: String): Future[Boolean] = {
    Auths.checkUserRegistered(phoneNumber)
  }

}

object CassandraDb extends CassandraDb(Connector.connector)