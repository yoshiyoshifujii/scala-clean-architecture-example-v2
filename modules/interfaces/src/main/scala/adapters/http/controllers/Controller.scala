package adapters.http.controllers

import adapters.http.directives.ValidateDirectives
import adapters.http.json.SignUpRequestJson
import adapters.http.presenters.SignUpPresenter
import adapters.{ AppType, Effect }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import usecases.anonymous.SignUpUseCase
import wvlet.airframe._

trait Controller {
  import ValidateDirectives._
  import adapters.errors.Errors._

  private implicit val runtime: scalaz.zio.Runtime[AppType] = bind[scalaz.zio.Runtime[AppType]]

  private val signUpUseCase   = bind[SignUpUseCase[Effect]]
  private val signUpPresenter = bind[SignUpPresenter]

  def toRoutes: Route =
    signUp

  private[controllers] def signUp: Route =
    path("accounts") {
      post {
        entity(as[SignUpRequestJson]) { json =>
          validateJsonRequest(json).apply { inputData =>
            signUpPresenter.response(signUpUseCase.execute(inputData))
          }
        }
      }
    }
}
