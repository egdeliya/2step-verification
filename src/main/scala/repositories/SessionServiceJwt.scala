package repositories

import exceptions.WrongSessionException
import pdi.jwt.JwtSession
import play.api.Configuration

import scala.concurrent.Future

class SessionServiceJwt(private val conf: Configuration) extends SessionService {

  def getSession(phoneNum: String): Future[String] = {
    Future.successful(
      (JwtSession()(conf) + ("userPhone", phoneNum)).serialize
    )
  }

  def getUserDataFromSessionToken(token: String): Future[String] = {
    JwtSession.deserialize(token)(conf)
      .get("userPhone")
      .map(phone => Future.successful(phone.toString()))
      .getOrElse(Future.failed(throw WrongSessionException()))
  }

}
