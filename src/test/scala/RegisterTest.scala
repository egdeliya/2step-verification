
import Cassandra.CassandraDb
import repositories._
import com.typesafe.config.Config
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.any
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import domain.models.User

import scala.concurrent.Future

class RegisterTest extends FlatSpec
  with MockitoSugar
  with Matchers
  with WebProtocol
  with ScalatestRouteTest {

  trait mocks {
    val conf: Config = mock[Config]

    val dbManager: CassandraDb = mock[CassandraDb]
    val bannedUsersService: BannedUsersService = mock[BannedUsersServiceDummy]

    val authService: AuthService = new AuthServiceCassandra(dbManager, bannedUsersService)
    val webServer = new WebServer(authService, conf)
    lazy val route: Route = webServer.getRoute()

    val validPhoneNumber = "79859234501"
    val invalidPhoneNumber = "89859234501"
    val validPassword = "12312Qwerty"
    val invalidPassword = "12312qwerty"
  }

  "WebServer" should "register user successfully" in new mocks {
    val newUser = User(validPhoneNumber, validPassword)

    when(bannedUsersService.checkUserIsBanned(newUser)).thenReturn(Future.successful(false))
    when(dbManager.checkUserRegistered(newUser.getPhone)).thenReturn(Future.successful(false))
    when(dbManager.registerUser(newUser.getPhone, newUser.getPass)).thenReturn(Future.successful())

    val registerRequest = RegisterRequest(validPhoneNumber, validPassword)
    Post("/register", registerRequest) ~> route ~> check {
      responseAs[String] shouldEqual WebStatus.Ok
      status shouldBe StatusCodes.Created
    }
  }

  "WebServer" should "fail registering user with incorrect phoneNumber" in new mocks {
    val registerRequest = RegisterRequest(invalidPhoneNumber, validPassword)
    Post("/register", registerRequest) ~> route ~> check {
      assert(responseAs[String] == s"Phone number should match '79*********' pattern")
      status shouldBe StatusCodes.BadRequest
    }

    verify(bannedUsersService, never()).checkUserIsBanned(any())
    verify(dbManager, never()).registerUser(any(), any())
    verify(dbManager, never()).checkUserRegistered(any())
  }

  "WebServer" should "fail registering user with weak password" in new mocks {
    val registerRequest = RegisterRequest(validPhoneNumber, invalidPassword)
    Post("/register", registerRequest) ~> route ~> check {
      assert(responseAs[String] == s"Password should contain at least one upper case symbol")
      status shouldBe StatusCodes.BadRequest
    }

    verify(bannedUsersService, never()).checkUserIsBanned(any())
    verify(dbManager, never()).registerUser(any(), any())
    verify(dbManager, never()).checkUserRegistered(any())
  }

  "WebServer" should "fail registering banned user" in new mocks {
    val newUser = User(validPhoneNumber, validPassword)

    when(bannedUsersService.checkUserIsBanned(newUser)).thenReturn(Future.successful(true))

    val registerRequest = RegisterRequest(validPhoneNumber, validPassword)
    Post("/register", registerRequest) ~> route ~> check {
      assert(responseAs[String] == s"User is banned")
      status shouldBe StatusCodes.BadRequest
    }

    verify(dbManager, never()).registerUser(any(), any())
    verify(dbManager, never()).checkUserRegistered(any())
  }

  "WebServer" should "fail registering user that already exists" in new mocks {
    val newUser = User(validPhoneNumber, validPassword)

    when(bannedUsersService.checkUserIsBanned(newUser)).thenReturn(Future.successful(false))
    when(dbManager.checkUserRegistered(newUser.getPhone)).thenReturn(Future.successful(true))

    val registerRequest = RegisterRequest(validPhoneNumber, validPassword)
    Post("/register", registerRequest) ~> route ~> check {
      assert(responseAs[String] == s"User ${newUser.getPhone} already registered")
      status shouldBe StatusCodes.BadRequest
    }

    verify(dbManager, never()).registerUser(any(), any())
  }
}
