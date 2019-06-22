package adapters.http.directives

import adapters.{ AppType, Effect }
import akka.http.scaladsl.model.headers.{ HttpChallenge, OAuth2BearerToken }
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.AuthenticationResult
import domain.account.Auth
import scalaz.zio.ZIO
import services.TokenService
import usecases.{ UseCaseApplicationError, UseCaseSystemError }

import scala.concurrent.Future

trait AuthDirectives {

  def validateAuth(
      implicit tokenService: TokenService[Effect],
      runtime: scalaz.zio.Runtime[AppType]
  ): Directive1[Auth] =
    authenticateOrRejectWithChallenge[OAuth2BearerToken, Auth] {
      case Some(bearerToken) =>
        runtime.unsafeRunToFuture {
          tokenService
            .verify(bearerToken.token, 0)
            .map(AuthenticationResult.success)
            .foldM(
              {
                case appError: UseCaseApplicationError => ZIO.fail(new RuntimeException(appError.toString))
                case UseCaseSystemError(cause)         => ZIO.fail(cause)

              },
              ZIO.succeed
            )
        }
      case None =>
        Future.successful(
          AuthenticationResult.failWithChallenge(HttpChallenge("bearer", None, Map("error" -> "invalid_token")))
        )
    }

}

object AuthDirectives extends AuthDirectives
