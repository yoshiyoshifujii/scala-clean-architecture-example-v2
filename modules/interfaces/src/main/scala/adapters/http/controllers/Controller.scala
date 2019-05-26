package adapters.http.controllers

import adapters.Effect
import adapters.http.directives.ValidateDirectives
import adapters.http.json.CreateAccountRequestJson
import adapters.http.presenters.CreateAccountPresenter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import usecases.account.CreateAccountUseCase
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import wvlet.airframe._

trait Controller extends ValidateDirectives {

  private val createAccountUseCase: CreateAccountUseCase[Effect]     = bind[CreateAccountUseCase[Effect]]
  private val createAccountPresenter: CreateAccountPresenter[Effect] = bind[CreateAccountPresenter[Effect]]

  def toRoutes: Route =
    createAccount

  private[controllers] def createAccount: Route =
    path("accounts") {
      post {
        entity(as[CreateAccountRequestJson]) { json =>
          createAccountPresenter.response(createAccountUseCase.execute(json))
        }
        ???
      }
    }
}
