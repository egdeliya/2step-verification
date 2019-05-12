package repositories

import Cassandra.CassandraDb
import com.nexmo.client.sms.messages.TextMessage
import com.nexmo.client.sms.{MessageStatus, SmsClient, SmsSubmissionResponse}
import domain.models.User
import exceptions.InternalServerErrorException

import scala.concurrent.Future
import scala.util.Random

import scala.concurrent.ExecutionContext.Implicits.global

class SmsServiceCassandraNexmo(private val database: CassandraDb,
                               private val smsClient: SmsClient)
  extends SmsService {

  private val serviceName = "2step-verification"
  private val start = 1000
  private val end = 9999
  private val rnd = new Random()

  def sendCode(user: User): Future[String] = {
    val smsCode = genSmsCode()
    val message = new TextMessage(
      serviceName,
      user.getPhone,
      smsCode
    )

    Future {
      val responses: SmsSubmissionResponse = smsClient.submitMessage(message)
      if (responses.getMessages.get(0).getStatus != MessageStatus.OK) {
        throw InternalServerErrorException()
      }
    }.flatMap { _ =>
      database.storeSmsCode(user.getPhone, smsCode)
    }
  }

  private def genSmsCode(): String = {
    (start + rnd.nextInt( (end - start) + 1 )).toString
  }
}
