
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.nexmo.client.sms.SmsSubmissionResponse
import domain.models.User
import org.mindrot.jbcrypt.BCrypt
import org.mockito.Matchers.any
import org.mockito.Matchers.{eq => matchEq}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class LoginTest extends FlatSpec
  with MockitoSugar
  with Matchers
  with WebProtocol
  with ScalatestRouteTest {

  trait loginTestData extends Mocks {
    val validPhoneNumber = "79859234501"
    val validPassword = "12312Qwerty"

    val smsCode = "1234"
    val smsSubmissionResponse: SmsSubmissionResponse = SmsSubmissionResponse.fromJson(
      """
        |{
        | "message-count":1,
        |   "messages":[
        |     {
        |       "status":0
        |     }
        |   ]
        |}
      """.stripMargin
    )
  }

  "WebServer" should "login user successfully" in new loginTestData {
    val newUser = User(validPhoneNumber, validPassword)

    when(redisClients.withClient(any())).thenReturn(None)
    when(dbManager.checkUserRegistered(newUser.getPhone))
      .thenReturn(Future.successful(Some(User(newUser.getPhone, BCrypt.hashpw(newUser.getPass, BCrypt.gensalt())))))
    when(smsClient.submitMessage(any())).thenReturn(smsSubmissionResponse)
    when(dbManager.storeSmsCode(matchEq(newUser.getPhone), any())).thenReturn(Future.successful(smsCode))

    val loginRequest = LoginRequest(validPhoneNumber, validPassword)
    Post("/login", loginRequest) ~> route ~> check {
      responseAs[String] shouldEqual WebStatus.Ok
      status shouldBe StatusCodes.OK
    }

    verify(redisClients).withClient(any())
    verify(smsClient).submitMessage(any())
    verify(dbManager).storeSmsCode(matchEq(newUser.getPhone), any())
  }

  "WebServer" should "fail logging banned user" in new loginTestData {
    val newUser = User(validPhoneNumber, validPassword)

    when(redisClients.withClient(any())).thenReturn(Some("banned"))

    val loginRequest = LoginRequest(validPhoneNumber, validPassword)
    Post("/login", loginRequest) ~> route ~> check {
      assert(responseAs[String] == s"User is banned")
      status shouldBe StatusCodes.BadRequest
    }

    verify(redisClients).withClient(any())
    verify(smsClient, never()).submitMessage(any())
    verify(dbManager, never()).storeSmsCode(any(), any())
    verify(dbManager, never()).checkUserRegistered(any())
  }

  "WebServer" should "fail logging user that was not registered" in new loginTestData {
    val newUser = User(validPhoneNumber, validPassword)

    when(redisClients.withClient(any())).thenReturn(None)
    when(dbManager.checkUserRegistered(newUser.getPhone)).thenReturn(Future.successful(None))

    val loginRequest = LoginRequest(validPhoneNumber, validPassword)
    Post("/login", loginRequest) ~> route ~> check {
      assert(responseAs[String] == s"User ${newUser.getPhone} not registered yet")
      status shouldBe StatusCodes.BadRequest
    }

    verify(redisClients).withClient(any())
    verify(smsClient, never()).submitMessage(any())
    verify(dbManager, never()).storeSmsCode(any(), any())
  }
}
