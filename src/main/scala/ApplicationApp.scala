import com.typesafe.config.ConfigFactory
import play.api.Configuration

import Cassandra.CassandraDb
import Cassandra.Connector._
import com.nexmo.client.NexmoClient
import com.redis.RedisClientPool
import repositories._

import com.timgroup.statsd.StatsDClient
import com.timgroup.statsd.NonBlockingStatsDClient

object ApplicationApp extends App {

  val conf = Configuration(ConfigFactory.load("application.conf"))
  val dbManager = new CassandraDb(connector)
  dbManager.createTablesIfNotExists()

  val redisEndpoint = conf.get[String]("redis.endpoint")
  val redisPort = conf.get[Int]("redis.port")
  val redisClients = new RedisClientPool(redisEndpoint, redisPort)
  val bannedUsersService: BannedUsersService = new BannedUsersServiceRedis(redisClients)

  val statsdEndpoint = conf.get[String]("statsd.endpoint")
  val statsdPort = conf.get[Int]("statsd.port")
  val statsdPrefix = conf.get[String]("statsd.prefix")
  private val statsd = new NonBlockingStatsDClient(statsdPrefix, statsdEndpoint, statsdPort)

  val nexmoApiKey = conf.get[String]("nexmo.api_key")
  val nexmoApiSecret = conf.get[String]("nexmo.api_secret")
  val smsClient = NexmoClient.builder()
    .apiKey(nexmoApiKey)
    .apiSecret(nexmoApiSecret)
    .build()
    .getSmsClient

  val smsService: SmsService = new SmsServiceCassandraNexmo(dbManager, smsClient)
  val authService = new AuthServiceCassandra(dbManager, bannedUsersService, smsService)

  val sessionService: SessionService = new SessionServiceJwt(conf)
  val webServer = new WebServer(authService, conf, sessionService, statsd)
  webServer.run()
}