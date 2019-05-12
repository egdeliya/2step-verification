
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.headers.{ HttpCookie, `Set-Cookie` }
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class VerifyCodeTest extends FlatSpec
  with MockitoSugar
  with Matchers
  with WebProtocol
  with ScalatestRouteTest {

  trait verifyCodeTestData extends Mocks {
    val validPhoneNumber = "79859234501"
    val smsCode = "1234"
    val wrongCode = "12345"
    val sessionToken = "12345"
  }

  "WebServer" should "verify code successfully" in new verifyCodeTestData {
    when(dbManager.getSmsCode(validPhoneNumber))
      .thenReturn(Future.successful(Some((validPhoneNumber, smsCode))))
    when(sessionService.getSession(validPhoneNumber))
      .thenReturn(Future.successful(sessionToken))

    val verifyRequest = VerifyRequest(validPhoneNumber, smsCode)
    Post("/verifyCode", verifyRequest) ~> route ~> check {
      responseAs[String] shouldEqual WebStatus.Ok
      header[`Set-Cookie`] shouldEqual Some(`Set-Cookie`(HttpCookie("session_token", value = sessionToken)))
      status shouldBe StatusCodes.OK
    }

    verify(redisClients, never()).withClient(any())
  }

  "WebServer" should "fail verifying wrong code" in new verifyCodeTestData {
    when(dbManager.getSmsCode(validPhoneNumber))
      .thenReturn(Future.successful(Some((validPhoneNumber, smsCode))))
    when(redisClients.withClient(any())).thenReturn(true)

    val verifyRequest = VerifyRequest(validPhoneNumber, wrongCode)
    Post("/verifyCode", verifyRequest) ~> route ~> check {
      responseAs[String] shouldEqual "Invalid code!"
      status shouldBe StatusCodes.BadRequest
      assert(header[`Set-Cookie`].isEmpty)
    }

    verify(sessionService, never).getSession(any())
  }

  "WebServer" should "fail if code was not found" in new verifyCodeTestData {
    when(dbManager.getSmsCode(validPhoneNumber))
      .thenReturn(Future.successful(None))

    val verifyRequest = VerifyRequest(validPhoneNumber, wrongCode)
    Post("/verifyCode", verifyRequest) ~> route ~> check {
      responseAs[String] shouldEqual "No code was found for user!"
      status shouldBe StatusCodes.BadRequest
      assert(header[`Set-Cookie`].isEmpty)
    }

    verify(redisClients, never).withClient(any())
    verify(sessionService, never).getSession(any())
  }

  "WebServer" should "fail if redis can't ban user with wrong code" in new verifyCodeTestData {
    when(dbManager.getSmsCode(validPhoneNumber))
      .thenReturn(Future.successful(Some((validPhoneNumber, smsCode))))
    when(redisClients.withClient(any())).thenReturn(false)

    val verifyRequest = VerifyRequest(validPhoneNumber, wrongCode)
    Post("/verifyCode", verifyRequest) ~> route ~> check {
      responseAs[String] shouldEqual "Some error occurred :( "
      status shouldBe StatusCodes.BadRequest
      assert(header[`Set-Cookie`].isEmpty)
    }

    verify(sessionService, never).getSession(any())
  }
}
