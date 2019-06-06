package adapters.http.controllers

import java.nio.charset.StandardCharsets

import adapters.dao.jdbc.RDB
import adapters.gateway.repositories.memory.AccountRepositoryByMemory
import adapters.http.json.CreateAccountResponseJson
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
      .bind[AccountRepository[Effect]].to[AccountRepositoryByMemory]
      .add(DISettings.designOfServices)

  "Controller" - {
    "createAccount" in {
      import io.circe.generic.auto._
      val controller = session.build[Controller]

      val data: Array[Byte] =
        """{"email":"a@a.com","name":"hoge hogeo","password":"hogeHOGE1"}""".getBytes(StandardCharsets.UTF_8)

      Post("/accounts", HttpEntity(ContentTypes.`application/json`, data)) ~> controller.createAccount ~> check {
        assert(response.status === StatusCodes.OK)
        val responseJson = responseAs[CreateAccountResponseJson]
        assert(responseJson.id.isDefined === true)
      }

      Post("/accounts", HttpEntity(ContentTypes.`application/json`, data)) ~> controller.createAccount ~> check {
        assert(response.status === StatusCodes.BadRequest)
      }

    }
  }

}
