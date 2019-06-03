package adapters

import adapters.gateway.repositories.slick.AccountRepositoryBySlick
import adapters.gateway.services.EncryptServiceByBCrypt
import adapters.http.controllers.Controller
import adapters.http.presenters.CreateAccountPresenter
import repositories.AccountRepository
import scalaz.zio.internal.{ Platform, PlatformLive }
import services.EncryptService
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

  private[adapters] def designOfServices: Design =
    newDesign
      .bind[EncryptService[Effect]].to[EncryptServiceByBCrypt]

  private[adapters] def designOfHttpPresenters: Design =
    newDesign
      .bind[CreateAccountPresenter].toEagerSingleton

  private[adapters] def designOfHttpControllers: Design =
    newDesign
      .bind[Controller].toEagerSingleton

  def design(environment: AppType): Design =
    designOfRuntime(environment)
      .add(designOfSlick(environment.rdbService.profile, environment.rdbService.db))
      .add(designOfRepositories)
      .add(designOfServices)

}

object DISettings extends DISettings
