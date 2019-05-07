import com.typesafe.config.ConfigFactory
import Cassandra.CassandraDb
import Cassandra.Connector._
import repositories.{AuthServiceCassandra, BannedUsersService, BannedUsersServiceDummy}

object ApplicationApp extends App {

  val conf = ConfigFactory.load()
  val dbManager = new CassandraDb(connector)
  dbManager.createTablesIfNotExists()

  val bannedUsersService: BannedUsersService = new BannedUsersServiceDummy

  val authService = new AuthServiceCassandra(dbManager, bannedUsersService)

  val webServer = new WebServer(authService, conf)
  webServer.run()
}