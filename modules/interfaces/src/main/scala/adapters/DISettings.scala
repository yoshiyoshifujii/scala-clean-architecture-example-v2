package adapters

import adapters.gateway.repositories.slick.AccountRepositoryBySlick
import adapters.gateway.services.{ EncryptServiceByBCrypt, JwtConfig, TokenServiceByJwt }
import com.auth0.jwt.algorithms.Algorithm
import repositories.AccountRepository
import scalaz.zio.internal.{ Platform, PlatformLive }
import services.{ EncryptService, TokenService }
import slick.jdbc.JdbcProfile
import wvlet.airframe._

trait DISettings {

  private[adapters] def designOfRuntime(environment: AppType): Design =
    newDesign
      .bind[scalaz.zio.Runtime[AppType]].toLazyInstance {
        new scalaz.zio.Runtime[AppType] {
          override val Environment: AppType = environment
          val Platform: Platform            = PlatformLive.Default
        }
      }

  private[adapters] def designOfSlick(profile: JdbcProfile, db: JdbcProfile#Backend#Database): Design =
    newDesign
      .bind[JdbcProfile].toInstance(profile)
      .bind[JdbcProfile#Backend#Database].toInstance(db)

  private[adapters] def designOfRepositories: Design =
    newDesign
      .bind[AccountRepository[Effect]].to[AccountRepositoryBySlick]

  private[adapters] def designOfJwtConfig(secret: String, jwtConfig: JwtConfig): Design =
    newDesign
      .bind[Algorithm].toLazyInstance(Algorithm.HMAC512(secret))
      .bind[JwtConfig].toLazyInstance(jwtConfig)

  private[adapters] def designOfServices: Design =
    newDesign
      .bind[EncryptService[Effect]].to[EncryptServiceByBCrypt]
      .bind[TokenService[Effect]].to[TokenServiceByJwt]

  def design(environment: AppType, jwtSecret: String, jwtConfig: JwtConfig): Design =
    designOfRuntime(environment)
      .add(designOfSlick(environment.rdbService.profile, environment.rdbService.db))
      .add(designOfRepositories)
      .add(designOfJwtConfig(jwtSecret, jwtConfig))
      .add(designOfServices)

}

object DISettings extends DISettings
