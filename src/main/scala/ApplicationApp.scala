import com.typesafe.config.ConfigFactory
import Cassandra.CassandraDb
import Cassandra.Connector._
import com.redis.RedisClientPool
import repositories.{AuthServiceCassandra, BannedUsersService, BannedUsersServiceDummy, BannedUsersServiceRedis}

object ApplicationApp extends App {

  val conf = ConfigFactory.load()
  val dbManager = new CassandraDb(connector)
  dbManager.createTablesIfNotExists()

  val endpoint = conf.getString("redis.endpoint")
  val port = conf.getInt("redis.port")
  val redisClients = new RedisClientPool(endpoint, port)

  val bannedUsersService: BannedUsersService = new BannedUsersServiceRedis(redisClients)

  val authService = new AuthServiceCassandra(dbManager, bannedUsersService)

  val webServer = new WebServer(authService, conf)
  webServer.run()
}