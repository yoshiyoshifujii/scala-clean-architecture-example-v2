package adapters.http.controllers

import java.nio.charset.StandardCharsets

import adapters.dao.jdbc.RDB
import adapters.gateway.repositories.memory.zio.AccountRepositoryByMemoryWithZIO
import adapters.gateway.services.JwtConfig
import adapters.http.json.{ SignInResponseJson, SignUpResponseJson }
import adapters.http.utils.RouteSpec
import adapters.{ AppType, DISettings, Effect }
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import com.auth0.jwt.algorithms.Algorithm
import org.scalatest.{ DiagrammedAssertions, FreeSpec }
import repositories.AccountRepository
import wvlet.airframe.Design

import scala.concurrent.duration._

class ControllerSpec extends FreeSpec with RouteSpec with DiagrammedAssertions {

  override def environment: AppType = new RDB.Live(null, null)

  override def design: Design =
    super.design
      .bind[AccountRepository[Effect]].to[AccountRepositoryByMemoryWithZIO]
      .add(DISettings.designOfServices)
      .bind[Algorithm].toInstance(Algorithm.HMAC512("secret"))
      .bind[JwtConfig].toInstance(
        JwtConfig(
          issuer = "sample",
          audience = "sample",
          accessTokenValueExpiresIn = 30.minutes.toMillis.millis
        )
      )

  "Controller" - {
    "Sign up and Sign in" in {
      import io.circe.generic.auto._
      val controller = session.build[Controller]

      val signUpData: Array[Byte] =
        """{"email":"a@a.com","name":"hoge hogeo","password":"hogeHOGE1"}""".getBytes(StandardCharsets.UTF_8)
      val accountId = Post("/signup", HttpEntity(ContentTypes.`application/json`, signUpData)) ~> controller.signUp ~> check {
          assert(response.status === StatusCodes.OK)
          val responseJson = responseAs[SignUpResponseJson]
          assert(responseJson.id.isDefined === true)
          responseJson.id.get
        }

      val signInData: Array[Byte] =
        """{"email":"a@a.com","password":"hogeHOGE1"}""".getBytes(StandardCharsets.UTF_8)
      Post("/signin", HttpEntity(ContentTypes.`application/json`, signInData)) ~> controller.signIn ~> check {
        assert(response.status === StatusCodes.OK)
        val responseJson = responseAs[SignInResponseJson]

        val maybeToken = responseJson.token
        assert(maybeToken.isDefined === true)

        val updateAccountData =
          """{"name":"fuga fugao"}""".getBytes(StandardCharsets.UTF_8)
        Post(s"/accounts/$accountId", HttpEntity(ContentTypes.`application/json`, updateAccountData))
          .addCredentials(OAuth2BearerToken(maybeToken.get)) ~> controller.updateAccount ~> check {
          assert(response.status === StatusCodes.OK)
        }

      }

    }
  }

}
