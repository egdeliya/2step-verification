
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.any
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.StatusCodes
import domain.models.User

import scala.concurrent.Future

class RegisterTest extends FlatSpec
  with MockitoSugar
  with Matchers
  with WebProtocol
  with ScalatestRouteTest {

  trait registerTestData extends Mocks {
    val validPhoneNumber = "79859234501"
    val invalidPhoneNumber = "89859234501"
    val validPassword = "12312Qwerty"
    val invalidPassword = "12312qwerty"
  }

  "WebServer" should "register user successfully" in new registerTestData {
    val newUser = User(validPhoneNumber, validPassword)

    when(redisClients.withClient(any())).thenReturn(None)
    when(dbManager.checkUserRegistered(newUser.getPhone)).thenReturn(Future.successful(None))
    when(dbManager.registerUser(newUser.getPhone, newUser.getPass)).thenReturn(Future.successful())

    val registerRequest = RegisterRequest(validPhoneNumber, validPassword)
    Post("/register", registerRequest) ~> route ~> check {
      responseAs[String] shouldEqual WebStatus.Ok
      status shouldBe StatusCodes.Created
    }

    verify(redisClients).withClient(any())
  }

  "WebServer" should "fail registering user with incorrect phoneNumber" in new registerTestData {
    val registerRequest = RegisterRequest(invalidPhoneNumber, validPassword)
    Post("/register", registerRequest) ~> route ~> check {
      assert(responseAs[String] == s"Phone number should match '79*********' pattern")
      status shouldBe StatusCodes.BadRequest
    }

    verify(redisClients, never).withClient(any())
    verify(dbManager, never()).registerUser(any(), any())
    verify(dbManager, never()).checkUserRegistered(any())
  }

  "WebServer" should "fail registering user with weak password" in new registerTestData {
    val registerRequest = RegisterRequest(validPhoneNumber, invalidPassword)
    Post("/register", registerRequest) ~> route ~> check {
      assert(responseAs[String] == s"Password should contain at least one upper case symbol")
      status shouldBe StatusCodes.BadRequest
    }

    verify(redisClients, never).withClient(any())
    verify(dbManager, never()).registerUser(any(), any())
    verify(dbManager, never()).checkUserRegistered(any())
  }

  "WebServer" should "fail registering banned user" in new registerTestData {
    val newUser = User(validPhoneNumber, validPassword)

    when(redisClients.withClient(any())).thenReturn(Some("banned"))

    val registerRequest = RegisterRequest(validPhoneNumber, validPassword)
    Post("/register", registerRequest) ~> route ~> check {
      assert(responseAs[String] == s"User is banned")
      status shouldBe StatusCodes.BadRequest
    }

    verify(redisClients).withClient(any())
    verify(dbManager, never()).registerUser(any(), any())
    verify(dbManager, never()).checkUserRegistered(any())
  }

  "WebServer" should "fail registering user that already exists" in new registerTestData {
    val newUser = User(validPhoneNumber, validPassword)

    when(redisClients.withClient(any())).thenReturn(None)
    when(dbManager.checkUserRegistered(newUser.getPhone)).thenReturn(Future.successful(Some(newUser)))

    val registerRequest = RegisterRequest(validPhoneNumber, validPassword)
    Post("/register", registerRequest) ~> route ~> check {
      assert(responseAs[String] == s"User ${newUser.getPhone} already registered")
      status shouldBe StatusCodes.BadRequest
    }

    verify(redisClients).withClient(any())
    verify(dbManager, never()).registerUser(any(), any())
  }
}
