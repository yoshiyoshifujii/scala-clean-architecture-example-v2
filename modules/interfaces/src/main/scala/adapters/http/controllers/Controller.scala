package adapters.http.controllers

import adapters.http.directives.ValidateDirectives
import adapters.http.json.CreateAccountRequestJson
import adapters.http.presenters.CreateAccountPresenter
import adapters.{ AppType, Effect }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import usecases.account.CreateAccountUseCase
import wvlet.airframe._

trait Controller {
  import ValidateDirectives._
  import adapters.errors.Errors._

  private implicit val runtime: scalaz.zio.Runtime[AppType] = bind[scalaz.zio.Runtime[AppType]]

  private val createAccountUseCase   = bind[CreateAccountUseCase[Effect]]
  private val createAccountPresenter = bind[CreateAccountPresenter]

  def toRoutes: Route =
    createAccount

  private[controllers] def createAccount: Route =
    path("accounts") {
      post {
        entity(as[CreateAccountRequestJson]) { json =>
          validateJsonRequest(json).apply { inputData =>
            createAccountPresenter.response(createAccountUseCase.execute(inputData))
          }
        }
      }
    }
}
