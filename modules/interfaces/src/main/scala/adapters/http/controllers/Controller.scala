package adapters.http.controllers

import adapters.http.directives.AuthDirectives
import adapters.http.directives.ValidateDirectives
import adapters.http.json.{
  AccountUpdateRequestJson,
  AccountUpdateRequestJsonWithId,
  SignInRequestJson,
  SignUpRequestJson
}
import adapters.http.presenters.{ SignInPresenter, SignUpPresenter, UpdateAccountPresenter }
import adapters.{ AppType, Effect }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import services.TokenService
import usecases.anonymous.{ SignInUseCase, SignUpUseCase }
import usecases.signed.UpdateAccountUseCase
import wvlet.airframe._

trait Controller {
  import ValidateDirectives._
  import adapters.errors.Errors._

  private implicit val runtime: scalaz.zio.Runtime[AppType] = bind[scalaz.zio.Runtime[AppType]]
  private implicit val tokenService: TokenService[Effect]   = bind[TokenService[Effect]]

  private val signUpUseCase   = bind[SignUpUseCase[Effect]]
  private val signUpPresenter = bind[SignUpPresenter]

  private val signInUseCase   = bind[SignInUseCase[Effect]]
  private val signInPresenter = bind[SignInPresenter]

  private val updateAccountUseCase   = bind[UpdateAccountUseCase[Effect]]
  private val updateAccountPresenter = bind[UpdateAccountPresenter]

  def toRoutes: Route =
    signUp ~ signIn

  private[controllers] def signUp: Route =
    path("signup") {
      post {
        entity(as[SignUpRequestJson]) { json =>
          validateJsonRequest(json).apply { inputData =>
            signUpPresenter.response(signUpUseCase.execute(inputData))
          }
        }
      }
    }

  private[controllers] def signIn: Route =
    path("signin") {
      post {
        entity(as[SignInRequestJson]) { json =>
          validateJsonRequest(json).apply { inputData =>
            signInPresenter.response(signInUseCase.execute(inputData))
          }
        }
      }
    }

  private[controllers] def updateAccount: Route =
    path("accounts" / Segment) { accountId =>
      post {
        AuthDirectives.validateAuth.apply { auth =>
          entity(as[AccountUpdateRequestJson]) { json =>
            validateJsonRequest(AccountUpdateRequestJsonWithId(auth, json, accountId)).apply { inputData =>
              updateAccountPresenter.response(updateAccountUseCase.execute(inputData))
            }
          }
        }
      }
    }
}
