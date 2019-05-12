import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import pdi.jwt.JwtSession
import play.api.Configuration
import com.typesafe.scalalogging.StrictLogging
import domain.models.User
import exceptions._
import repositories.{AuthService, SessionService}

import scala.io.StdIn

class WebServer(private val authService: AuthService,
                private val conf: Configuration,
                private val sessionService: SessionService
)
  extends Directives
  with WebProtocol
  with StrictLogging {

  private implicit val system = ActorSystem("2step-verification")
  private implicit val materializer = ActorMaterializer()
  private implicit val ec = system.dispatcher

  private val endpoint = conf.get[String]("webserver.endpoint")
  private val port = conf.get[Int]("webserver.port")

  private val route =
    handleExceptions(twoStepAuthExceptionHandler) {
      path("register") {
        post {
          entity(as[RegisterRequest]) { registerRequest =>
            val user = User(registerRequest.phoneNumber, registerRequest.password)
            logger.info(s"Registering in ${user.getPhone}")

            val result = authService.register(user)

            onSuccess(result) { ctx =>
              ctx.complete(HttpResponse(201, entity = WebStatus.Ok))
            }
          }
        }
      } ~
      path("login") {
        post {
          entity(as[LoginRequest]) { loginRequest =>
            val user = User(loginRequest.phoneNumber, loginRequest.password)
            logger.info(s"Logging in ${user.getPhone}")

            val res = authService.login(user)

            onSuccess(res) { _ =>
              complete(HttpResponse(200, entity = WebStatus.Ok))
            }
          }
        }
      } ~
        path("verifyCode") {
          post {
            entity(as[VerifyRequest]) { verifyRequest =>
              logger.info(s"User ${verifyRequest.phoneNumber} is verifying code")

              val sessionToken = authService
                .verifyCode(verifyRequest.phoneNumber, verifyRequest.code)
                .flatMap { _ =>
                  sessionService.getSession(verifyRequest.phoneNumber)
                }

              onSuccess(sessionToken) { sessionToken =>
                val cookie = HttpCookie("session_token", sessionToken)
                setCookie(cookie) { ctx =>
                  ctx.complete(HttpResponse(200, entity = WebStatus.Ok))
                }
              }
            }
          }
        }
    }

  def getRoute = route

  def run(): Unit = {
    val bindingFuture = Http().bindAndHandle(route, endpoint, port)
    println(s"Server online at http://$endpoint:$port/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ =>
        system.terminate())
  }

  private def twoStepAuthExceptionHandler: ExceptionHandler = ExceptionHandler {
    case th @ (UserAlreadyRegisteredException(_, _) |
               UserBannedException(_, _) |
               InvalidPhoneNumberException(_, _) |
               WeakPasswordException(_, _) |
               UserNotRegisteredException(_) |
               InvalidCodeException() |
               InternalServerErrorException()) =>
      complete(HttpResponse(400, entity = th.getMessage))
    case th: InternalError =>
      complete (HttpResponse(500, entity = th.getMessage))
    case th: Throwable =>
      logger.error(s"Error while doing request ${th.getMessage}")
//      complete (HttpResponse(500, entity = "Some error occurred while doing request"))
      complete (HttpResponse(500, entity = th.getMessage))
  }

}
