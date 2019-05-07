package Cassandra

import com.outworkers.phantom.connectors.{CassandraConnection, ContactPoints}
import com.typesafe.config.ConfigFactory

object Connector {
  private val config = ConfigFactory.load()

  private val hosts = config.getString("cassandra.host")
  private val keyspace = config.getString("cassandra.keyspace")

  lazy val connector: CassandraConnection = ContactPoints(List(hosts)).keySpace(keyspace)
}


