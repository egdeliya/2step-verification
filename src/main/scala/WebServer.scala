import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import play.api.Configuration
import com.typesafe.scalalogging.StrictLogging
import domain.models.User
import exceptions._
import repositories.{AuthService, SessionService}

import com.timgroup.statsd.NonBlockingStatsDClient

import scala.io.StdIn

class WebServer(private val authService: AuthService,
                private val conf: Configuration,
                private val sessionService: SessionService,
                private val statsd: NonBlockingStatsDClient
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
            statsd.incrementCounter("register")
            statsd.incrementCounter("requestInProgress")
            val start = System.currentTimeMillis()

            val user = User(registerRequest.phoneNumber, registerRequest.password)
            logger.info(s"Registering in ${user.getPhone}")

            val result = authService
              .register(user)
              .map {
                _ => statsd.recordExecutionTime("registerTime", System.currentTimeMillis() - start)
              }

            onSuccess(result) { ctx =>
              statsd.decrementCounter("requestInProgress")
              ctx.complete(HttpResponse(201, entity = WebStatus.Ok))
            }
          }
        }
      } ~
      path("login") {
        post {
          entity(as[LoginRequest]) { loginRequest =>
            statsd.incrementCounter("login")
            statsd.incrementCounter("requestInProgress")
            val start = System.currentTimeMillis()

            val user = User(loginRequest.phoneNumber, loginRequest.password)
            logger.info(s"Logging in ${user.getPhone}")

            statsd.incrementCounter("requestInProgress")
            val res = authService
              .login(user)
              .map {
                _ => statsd.recordExecutionTime("loginTime", System.currentTimeMillis() - start)
              }

            onSuccess(res) { ctx =>
              statsd.decrementCounter("requestInProgress")
              ctx.complete(HttpResponse(200, entity = WebStatus.Ok))
            }
          }
        }
      } ~
        path("verifyCode") {
          post {
            entity(as[VerifyRequest]) { verifyRequest =>
              statsd.incrementCounter("verifyCode")
              statsd.incrementCounter("requestInProgress")
              val start = System.currentTimeMillis()

              logger.info(s"User ${verifyRequest.phoneNumber} is verifying code")

              val sessionToken = authService
                .verifyCode(verifyRequest.phoneNumber, verifyRequest.code)
                .flatMap { _ =>
                  statsd.recordExecutionTime("verifyCodeTime", System.currentTimeMillis() - start)
                  sessionService.getSession(verifyRequest.phoneNumber)
                }

              onSuccess(sessionToken) { sessionToken =>
                statsd.decrementCounter("requestInProgress")
                val cookie = HttpCookie("session_token", sessionToken)
                setCookie(cookie) { ctx =>
                  ctx.complete(HttpResponse(200, entity = WebStatus.Ok))
                }
              }
            }
          }
        } ~
        path("") {
          statsd.incrementCounter("/")
          statsd.incrementCounter("requestInProgress")

          optionalCookie("session_token") {
            case Some(sessionToken) =>
              val start = System.currentTimeMillis()
              val userData = sessionService
                .getUserDataFromSessionToken(sessionToken.value)
                .map { data =>
                  statsd.recordExecutionTime("/ time", System.currentTimeMillis() - start)
                  data
                }

              onSuccess(userData) { userData =>
                statsd.decrementCounter("requestInProgress")
                complete(HttpResponse(200, entity = s"User $userData was successfully logged in!"))
              }

            case None =>
              statsd.decrementCounter("requestInProgress")
              complete(HttpResponse(StatusCodes.Forbidden, entity = "Try to login first!"))
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
               InternalServerErrorException() |
               NoCodeFoundException() |
               InvalidCredentialsException() |
               WrongSessionException()) =>
      statsd.incrementCounter("statusCode.400")
      statsd.decrementCounter("requestInProgress")

      complete(HttpResponse(400, entity = th.getMessage))
    case th: InternalError =>
      statsd.incrementCounter("statusCode.500")
      statsd.decrementCounter("requestInProgress")

      complete (HttpResponse(500, entity = th.getMessage))
    case th: Throwable =>
      statsd.incrementCounter("statusCode.500")
      statsd.decrementCounter("requestInProgress")

      logger.error(s"Error while doing request ${th.getMessage}")
//      complete (HttpResponse(500, entity = "Some error occurred while doing request"))
      complete (HttpResponse(500, entity = th.getMessage))
  }

}
