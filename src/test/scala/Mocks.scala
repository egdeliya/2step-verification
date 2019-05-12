import Cassandra.CassandraDb
import akka.http.scaladsl.server.Route
import com.nexmo.client.sms.SmsClient
import com.redis.RedisClientPool
import com.typesafe.config.Config
import org.scalatest.mockito.MockitoSugar
import repositories._

trait Mocks extends MockitoSugar {
  val conf: Config = mock[Config]

  val dbManager: CassandraDb = mock[CassandraDb]
  val redisClients: RedisClientPool = mock[RedisClientPool]
  val smsClient: SmsClient = mock[SmsClient]

  val smsService: SmsService = new SmsServiceCassandraNexmo(dbManager, smsClient)
  val bannedUsersService: BannedUsersService = new BannedUsersServiceRedis(redisClients)
  val authService: AuthService = new AuthServiceCassandra(dbManager, bannedUsersService, smsService)
  val webServer = new WebServer(authService, conf)
  lazy val route: Route = webServer.getRoute()
}
