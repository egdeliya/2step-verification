package akkaHttpSession

import com.softwaremill.session.{MultiValueSessionSerializer, SessionSerializer}

import scala.util.Try

case class UserSession(userPhone: String, authLevel: String)

object UserSession {
  implicit def serializer: SessionSerializer[UserSession, String] =
    new MultiValueSessionSerializer[UserSession](
      userSession => Map("userPhone" -> userSession.userPhone, "authLevel" -> userSession.authLevel),
      userSessionMap => Try { UserSession(userSessionMap("userPhone"), userSessionMap("authLevel")) }
    )
}
