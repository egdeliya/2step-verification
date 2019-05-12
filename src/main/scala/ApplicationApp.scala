import com.typesafe.config.ConfigFactory
import Cassandra.CassandraDb
import Cassandra.Connector._
import com.nexmo.client.NexmoClient
import com.redis.RedisClientPool
import repositories._

object ApplicationApp extends App {

  val conf = ConfigFactory.load()
  val dbManager = new CassandraDb(connector)
  dbManager.createTablesIfNotExists()

  val redisEndpoint = conf.getString("redis.endpoint")
  val redisPort = conf.getInt("redis.port")
  val redisClients = new RedisClientPool(redisEndpoint, redisPort)
  val bannedUsersService: BannedUsersService = new BannedUsersServiceRedis(redisClients)

  val nexmoApiKey = conf.getString("nexmo.api_key")
  val nexmoApiSecret = conf.getString("nexmo.api_secret")
  val smsClient = NexmoClient.builder()
    .apiKey(nexmoApiKey)
    .apiSecret(nexmoApiSecret)
    .build()
    .getSmsClient

  val smsService: SmsService = new SmsServiceCassandraNexmo(dbManager, smsClient)
  val authService = new AuthServiceCassandra(dbManager, bannedUsersService, smsService)

  val webServer = new WebServer(authService, conf)
  webServer.run()
}