package adapters.http.controllers

import java.nio.charset.StandardCharsets

import adapters.dao.jdbc.RDB
import adapters.gateway.services.JwtConfig
import adapters.http.json.{ AccountGetResponseJson, AccountGetsResponseJson, SignInResponseJson, SignUpResponseJson }
import adapters.http.utils.RouteSpec
import adapters.util.{ FlywayWithMySQLSpecSupport, Slick3SpecSupport }
import adapters.{ AppType, DISettings }
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import com.auth0.jwt.algorithms.Algorithm
import io.circe.generic.auto._
import org.scalatest.{ DiagrammedAssertions, FreeSpec }
import wvlet.airframe.Design

import scala.concurrent.duration._

class ControllerSpec
    extends FreeSpec
    with FlywayWithMySQLSpecSupport
    with Slick3SpecSupport
    with RouteSpec
    with DiagrammedAssertions {
  override val tables: Seq[String] = Seq("account")

  override def environment: AppType = new RDB.Live(null, null)

  override def design: Design =
    super.design
      .add(DISettings.designOfSlick(dbConfig.profile, dbConfig.db))
      .add(DISettings.designOfRepositories)
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
    lazy val controller = session.build[Controller]

    "success" in {
      val signUpData: Array[Byte] =
        """{"email":"a@a.com","name":"hoge hogeo","password":"hogeHOGE1"}""".getBytes(StandardCharsets.UTF_8)
      val accountId = Post("/signup", HttpEntity(ContentTypes.`application/json`, signUpData)) ~> controller.toRoutes ~> check {
          assert(response.status === StatusCodes.OK)
          val responseJson = responseAs[SignUpResponseJson]
          assert(responseJson.id.isDefined === true)
          responseJson.id.get
        }

      val signInData: Array[Byte] =
        """{"email":"a@a.com","password":"hogeHOGE1"}""".getBytes(StandardCharsets.UTF_8)
      Post("/signin", HttpEntity(ContentTypes.`application/json`, signInData)) ~> controller.toRoutes ~> check {
        assert(response.status === StatusCodes.OK)
        val responseJson = responseAs[SignInResponseJson]

        val maybeToken = responseJson.token
        assert(maybeToken.isDefined === true)

        Get("/accounts")
          .addCredentials(OAuth2BearerToken(maybeToken.get)) ~> controller.toRoutes ~> check {
          assert(response.status === StatusCodes.OK)

          val responseJson = responseAs[AccountGetsResponseJson]
          assert(responseJson.accounts.nonEmpty)
          assert(responseJson.accounts.head.id === accountId)
          assert(responseJson.accounts.head.name === "hoge hogeo")
        }

        val accountUpdateData =
          """{"name":"fuga fugao"}""".getBytes(StandardCharsets.UTF_8)
        Post(s"/accounts/$accountId", HttpEntity(ContentTypes.`application/json`, accountUpdateData))
          .addCredentials(OAuth2BearerToken(maybeToken.get)) ~> controller.toRoutes ~> check {
          assert(response.status === StatusCodes.OK)
        }

        Get(s"/accounts/$accountId")
          .addCredentials(OAuth2BearerToken(maybeToken.get)) ~> controller.toRoutes ~> check {
          assert(response.status === StatusCodes.OK)

          val responseJson = responseAs[AccountGetResponseJson]
          assert(responseJson.account.nonEmpty)
          assert(responseJson.account.get.id === accountId)
          assert(responseJson.account.get.name === "fuga fugao")
        }

        Delete(s"/accounts/$accountId")
          .addCredentials(OAuth2BearerToken(maybeToken.get)) ~> controller.toRoutes ~> check {
          assert(response.status === StatusCodes.OK)
        }

        Get(s"/accounts/$accountId")
          .addCredentials(OAuth2BearerToken(maybeToken.get)) ~> controller.toRoutes ~> check {
          assert(response.status === StatusCodes.NotFound)
        }

      }
    }

    "invalid case - sign up - already exists" in {
      val signUpData: Array[Byte] =
        """{"email":"b@b.com","name":"hoge hogeo","password":"hogeHOGE1"}""".getBytes(StandardCharsets.UTF_8)

      Post("/signup", HttpEntity(ContentTypes.`application/json`, signUpData)) ~> controller.toRoutes ~> check {
        assert(response.status === StatusCodes.OK)
        val responseJson = responseAs[SignUpResponseJson]
        assert(responseJson.id.isDefined === true)
      }

      Post("/signup", HttpEntity(ContentTypes.`application/json`, signUpData)) ~> controller.toRoutes ~> check {
        assert(response.status === StatusCodes.BadRequest)
      }
    }

    "invalid case - sign up - bad request" in {
      val signUpData1: Array[Byte] =
        """{"email":"","name":"hoge hogeo","password":"hogeHOGE1"}""".getBytes(StandardCharsets.UTF_8)
      Post("/signup", HttpEntity(ContentTypes.`application/json`, signUpData1)) ~> controller.toRoutes ~> check {
        assert(response.status === StatusCodes.BadRequest)
      }
      val signUpData2: Array[Byte] =
        """{"email":"c@c.com","name":"","password":"hogeHOGE1"}""".getBytes(StandardCharsets.UTF_8)
      Post("/signup", HttpEntity(ContentTypes.`application/json`, signUpData2)) ~> controller.toRoutes ~> check {
        assert(response.status === StatusCodes.BadRequest)
      }
      val signUpData3: Array[Byte] =
        """{"email":"c@c.com","name":"hoge hogeo","password":""}""".getBytes(StandardCharsets.UTF_8)
      Post("/signup", HttpEntity(ContentTypes.`application/json`, signUpData3)) ~> controller.toRoutes ~> check {
        assert(response.status === StatusCodes.BadRequest)
      }
    }

    "invalid case - sign in - bad request" in {
      val signInData1: Array[Byte] =
        """{"email":"z@z.com","password":"hogeHOGE1"}""".getBytes(StandardCharsets.UTF_8)
      Post("/signin", HttpEntity(ContentTypes.`application/json`, signInData1)) ~> controller.toRoutes ~> check {
        assert(response.status === StatusCodes.BadRequest)
      }
      val signInData2: Array[Byte] =
        """{"email":"","password":"hogeHOGE1"}""".getBytes(StandardCharsets.UTF_8)
      Post("/signin", HttpEntity(ContentTypes.`application/json`, signInData2)) ~> controller.toRoutes ~> check {
        assert(response.status === StatusCodes.BadRequest)
      }
      val signInData3: Array[Byte] =
        """{"email":"z@z.com","password":""}""".getBytes(StandardCharsets.UTF_8)
      Post("/signin", HttpEntity(ContentTypes.`application/json`, signInData3)) ~> controller.toRoutes ~> check {
        assert(response.status === StatusCodes.BadRequest)
      }
    }
  }

}
