import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class RegisterRequest(phoneNumber: String, password: String)
case class LoginRequest(phoneNumber: String, password: String)

trait WebProtocol extends SprayJsonSupport {
  import DefaultJsonProtocol._

  object WebStatus {
    val Ok = "OK"
    val Error = "Error"
  }

  implicit val registerRequestFormat = jsonFormat2(RegisterRequest)
  implicit val loginRequestFormat = jsonFormat2(LoginRequest)
}
