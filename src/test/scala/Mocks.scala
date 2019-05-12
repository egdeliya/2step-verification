import Cassandra.CassandraDb
import akka.http.scaladsl.server.Route
import com.nexmo.client.sms.SmsClient
import com.redis.RedisClientPool
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import repositories._
import org.mockito.Mockito._
import org.mockito.Matchers.any

import scala.concurrent.duration.Duration

trait Mocks extends MockitoSugar {
  val conf: Configuration = mock[Configuration]

  val dbManager: CassandraDb = mock[CassandraDb]
  val redisClients: RedisClientPool = mock[RedisClientPool]
  val smsClient: SmsClient = mock[SmsClient]

  val smsService: SmsService = new SmsServiceCassandraNexmo(dbManager, smsClient)
  val bannedUsersService: BannedUsersService = new BannedUsersServiceRedis(redisClients)
  val authService: AuthService = new AuthServiceCassandra(dbManager, bannedUsersService, smsService)

  val sessionService = mock[SessionService]
  val webServer = new WebServer(authService, conf, sessionService)
  lazy val route: Route = webServer.getRoute
}
