import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.{Directives, ExceptionHandler}
import akka.stream.ActorMaterializer

import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import domain.models.User
import exceptions.{InvalidPhoneNumberException, UserAlreadyRegisteredException, UserBannedException, WeakPasswordException}
import repositories.AuthService

import scala.io.StdIn

class WebServer(private val authService: AuthService,
                private val conf: Config
)
  extends Directives
  with WebProtocol
  with StrictLogging {

  private implicit val system = ActorSystem("2step-verification")
  private implicit val materializer = ActorMaterializer()
  private implicit val ec = system.dispatcher

  private val endpoint = conf.getString("webserver.endpoint")
  private val port = conf.getInt("webserver.port")

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
      }
    }

  def getRoute() = route

  def run() = {
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
               WeakPasswordException(_, _)) =>
      complete(HttpResponse(400, entity = th.getMessage))
    case th: InternalError =>
      complete (HttpResponse(500, entity = th.getMessage))
    case th: Throwable =>
      logger.error(s"Error while doing request ${th.getMessage}")
//      complete (HttpResponse(500, entity = "Some error occurred while doing request"))
      complete (HttpResponse(500, entity = th.getMessage))
  }

}
