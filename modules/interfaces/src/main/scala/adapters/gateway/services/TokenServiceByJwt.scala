package adapters.gateway.services

import java.time.{ Instant, ZoneId, ZonedDateTime }
import java.util.{ Date, UUID }

import adapters.{ AppType, Effect }
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import domain.account.{ AccountId, Auth }
import infrastructure.ulid.ULID
import scalaz.zio.ZIO
import services.TokenService
import usecases.{ UseCaseError, UseCaseSystemError }
import wvlet.airframe._

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

case class JwtConfig(issuer: String, audience: String, accessTokenValueExpiresIn: FiniteDuration)

trait TokenServiceByJwt extends TokenService[Effect] {

  private val algorithm: Algorithm = bind[Algorithm]
  private val config: JwtConfig    = bind[JwtConfig]

  override def generate(auth: Auth): Effect[String] =
    ZIO
      .fromTry {
        Try {
          val nowZdt      = nowRoundedToSecond
          val expiresZdt  = nowZdt.plusSeconds(config.accessTokenValueExpiresIn.toSeconds)
          val nowDate     = Date.from(nowZdt.toInstant)
          val expiresDate = Date.from(expiresZdt.toInstant)
          val jti         = UUID.randomUUID().toString

          JWT
            .create()
            .withHeader(Map[String, AnyRef]("typ" -> "JWT", "cty" -> "JWT").asJava)
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(auth.accountId.value)
            .withJWTId(jti)
            .withIssuedAt(nowDate)
            .withExpiresAt(expiresDate)
            .withClaim("account_id", auth.accountId.value)
            .sign(algorithm)
        }
      }.foldM[AppType, UseCaseError, String](
        cause => ZIO.fail(UseCaseSystemError(cause)),
        ZIO.succeed
      )

  override def verify(token: String, acceptExpiresAt: Long): Effect[Auth] =
    ZIO
      .fromTry {
        Try {
          JWT
            .require(algorithm)
            .acceptLeeway(1)
            .acceptExpiresAt(acceptExpiresAt)
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .build()
            .verify(token)
            .getSubject
        }.flatMap(ULID.parseFromString)
          .map(a => Auth(AccountId(a)))
      }.foldM[AppType, UseCaseError, Auth](
        cause => ZIO.fail(UseCaseSystemError(cause)),
        ZIO.succeed
      )

  private def nowRoundedToSecond: ZonedDateTime = {
    val nowZdt = ZonedDateTime.now(ZoneId.of("UTC"))
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(nowZdt.toEpochSecond), nowZdt.getZone)
  }

}
