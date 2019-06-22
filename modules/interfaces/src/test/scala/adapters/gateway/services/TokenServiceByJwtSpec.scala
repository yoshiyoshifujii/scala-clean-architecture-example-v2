package adapters.gateway.services

import adapters.dao.jdbc.RDB
import adapters.{ AppType, Effect }
import com.auth0.jwt.algorithms.Algorithm
import domain.account.{ AccountId, Auth }
import org.scalatest.{ DiagrammedAssertions, FreeSpec }
import scalaz.zio.internal.{ Platform, PlatformLive }
import services.TokenService
import wvlet.airframe._

import scala.concurrent.duration._

class TokenServiceByJwtSpec extends FreeSpec with DiagrammedAssertions {

  "TokenService" - {

    def runtime = new scalaz.zio.Runtime[AppType] {
      override val Environment: AppType = new RDB.Live(null, null)
      val Platform: Platform            = PlatformLive.Default
    }

    val jwtConfig = JwtConfig(
      issuer = "sample",
      audience = "sample",
      accessTokenValueExpiresIn = 30.minutes.toMillis.millis
    )
    val design =
      newDesign
        .bind[Algorithm].toInstance(Algorithm.HMAC512("secret"))
        .bind[JwtConfig].toInstance(jwtConfig)
        .bind[TokenService[Effect]].to[TokenServiceByJwt]

    val accountId = AccountId()

    "generate and verify" in {
      design.withSession { session =>
        val tokenService = session.build[TokenService[Effect]]
        val verifiedAccountId = runtime.unsafeRun {
          for {
            jwt <- tokenService.generate(Auth(accountId))
            aid <- tokenService.verify(jwt, 0)
          } yield aid
        }

        assert(verifiedAccountId.accountId === accountId)
      }

    }

  }

}
