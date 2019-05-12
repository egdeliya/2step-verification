package repositories

import com.redis
import com.redis.RedisClientPool
import com.typesafe.scalalogging.StrictLogging
import domain.models.User
import exceptions.InternalServerErrorException

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class BannedUsersServiceRedis(private val redisClients: RedisClientPool)
  extends BannedUsersService
  with StrictLogging {

  private val userIsBanned = "banned"
  private val durationPeriod = redis.Seconds(1.hour.toSeconds)

  def checkUserIsBanned(user: User): Future[Boolean] = {
    Future {
      redisClients.withClient { client =>
        client.get(user.getPhone)
      }
    }.map {
      case Some(_) => true
      case None => false
    }
  }

  def banUser(phoneNum: String): Future[Unit] = {
      Future {
        redisClients.withClient { client =>
          client.set(key = phoneNum,
            value = userIsBanned,
            onlyIfExists = false,
            time = durationPeriod)
        }
      }.flatMap {
        case successfulResult if successfulResult => Future.successful()
        case _ =>
          logger.error(s"Redis error, failed to ban user $phoneNum!")
          Future.failed(InternalServerErrorException())
      }
  }

}
