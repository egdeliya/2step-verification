
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Cookie
import akka.http.scaladsl.testkit.ScalatestRouteTest
import exceptions.WrongSessionException
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class EnterAppTest extends FlatSpec
  with MockitoSugar
  with Matchers
  with WebProtocol
  with ScalatestRouteTest {

  trait enterAppTestData extends Mocks {
    val sessionTokenCookie = "wfsjdajsdlh"
    val userPhone = "123123"
  }

  "WebServer" should "successfully login user" in new enterAppTestData {
    when(sessionService.getUserDataFromSessionToken(sessionTokenCookie))
        .thenReturn(Future.successful(userPhone))

    Get("/") ~> Cookie("session_token" -> sessionTokenCookie) ~> route ~> check {
      responseAs[String] shouldEqual s"User $userPhone was successfully logged in!"
      status shouldBe StatusCodes.OK
    }
  }

  "WebServer" should "fail login user with no cookie" in new enterAppTestData {
    Get("/") ~> route ~> check {
      responseAs[String] shouldEqual "Try to login first!"
      status shouldBe StatusCodes.Forbidden
    }

    verify(sessionService, never).getUserDataFromSessionToken(any())
  }

  "WebServer" should "fail login user with wrong cookie" in new enterAppTestData {
    when(sessionService.getUserDataFromSessionToken(sessionTokenCookie))
      .thenReturn(Future.failed(WrongSessionException()))

    Get("/") ~> Cookie("session_token" -> sessionTokenCookie) ~> route ~> check {
      responseAs[String] shouldEqual "Wrong session!"
      status shouldBe StatusCodes.BadRequest
    }
  }
}
