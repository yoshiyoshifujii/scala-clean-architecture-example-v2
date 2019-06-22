package adapters.http.controllers

import java.nio.charset.StandardCharsets

import adapters.dao.jdbc.RDB
import adapters.gateway.repositories.memory.zio.AccountRepositoryByMemoryWithZIO
import adapters.http.json.SignUpResponseJson
import adapters.http.utils.RouteSpec
import adapters.{ AppType, DISettings, Effect }
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import org.scalatest.FreeSpec
import repositories.AccountRepository
import wvlet.airframe.Design

class ControllerSpec extends FreeSpec with RouteSpec {

  override def environment: AppType = new RDB.Live(null, null)

  override def design: Design =
    super.design
      .bind[AccountRepository[Effect]].to[AccountRepositoryByMemoryWithZIO]
      .add(DISettings.designOfServices)

  "Controller" - {
    "sign up" in {
      import io.circe.generic.auto._
      val controller = session.build[Controller]

      val data: Array[Byte] =
        """{"email":"a@a.com","name":"hoge hogeo","password":"hogeHOGE1"}""".getBytes(StandardCharsets.UTF_8)

      Post("/signup", HttpEntity(ContentTypes.`application/json`, data)) ~> controller.signUp ~> check {
        assert(response.status === StatusCodes.OK)
        val responseJson = responseAs[SignUpResponseJson]
        assert(responseJson.id.isDefined === true)
      }

    }
  }

}
