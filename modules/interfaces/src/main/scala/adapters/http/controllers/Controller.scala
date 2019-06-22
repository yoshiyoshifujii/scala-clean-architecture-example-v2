package adapters.http.controllers

import adapters.http.directives.{ AuthDirectives, ValidateDirectives }
import adapters.http.json._
import adapters.http.presenters.{ AccountDeletePresenter, AccountUpdatePresenter, SignInPresenter, SignUpPresenter }
import adapters.{ AppType, Effect }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import services.TokenService
import usecases.anonymous.{ SignInUseCase, SignUpUseCase }
import usecases.signed.{ AccountDeleteUseCase, AccountUpdateUseCase }
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

  private val accountUpdateUseCase   = bind[AccountUpdateUseCase[Effect]]
  private val accountUpdatePresenter = bind[AccountUpdatePresenter]

  private val accountDeleteUseCase   = bind[AccountDeleteUseCase[Effect]]
  private val accountDeletePresenter = bind[AccountDeletePresenter]

  def toRoutes: Route =
    signUp ~ signIn ~ accountUpdate ~ accountDelete

  private def signUp: Route =
    path("signup") {
      post {
        entity(as[SignUpRequestJson]) { json =>
          validateJsonRequest(json).apply { inputData =>
            signUpPresenter.response(signUpUseCase.execute(inputData))
          }
        }
      }
    }

  private def signIn: Route =
    path("signin") {
      post {
        entity(as[SignInRequestJson]) { json =>
          validateJsonRequest(json).apply { inputData =>
            signInPresenter.response(signInUseCase.execute(inputData))
          }
        }
      }
    }

  private def accountUpdate: Route =
    path("accounts" / Segment) { accountId =>
      post {
        AuthDirectives.validateAuth.apply { auth =>
          entity(as[AccountUpdateRequestJson]) { json =>
            validateJsonRequest(AccountUpdateRequestJsonWithAuth(auth, json, accountId)).apply { inputData =>
              accountUpdatePresenter.response(accountUpdateUseCase.execute(inputData))
            }
          }
        }
      }
    }

  private def accountDelete: Route =
    path("accounts" / Segment) { accountId =>
      delete {
        AuthDirectives.validateAuth.apply { auth =>
          validateJsonRequest(AccountDeleteRequestWithAuth(auth, accountId)).apply { inputData =>
            accountDeletePresenter.response(accountDeleteUseCase.execute(inputData))
          }
        }
      }
    }
}
