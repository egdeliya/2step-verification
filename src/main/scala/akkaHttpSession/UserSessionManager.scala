package akkaHttpSession

import com.softwaremill.session.{BasicSessionEncoder, SessionConfig, SessionManager}
import com.typesafe.config.Config

class UserSessionManager(config: Config) {
  val sessionEncoder = new BasicSessionEncoder[UserSession]()(UserSession.serializer)
  val sessionConfig: SessionConfig = SessionConfig.fromConfig(config)
  val sessionManager = new SessionManager[UserSession](sessionConfig)(sessionEncoder)
}
