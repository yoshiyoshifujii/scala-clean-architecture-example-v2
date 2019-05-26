package adapters

import adapters.gateway.repositories.AccountRepositoryOnRDB
import adapters.gateway.services.EncryptServiceOnBCrypt
import adapters.http.controllers.Controller
import adapters.http.presenters.CreateAccountPresenter
import repositories.AccountRepository
import scalaz.zio.internal.{ Platform, PlatformLive }
import services.EncryptService
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

  private[adapters] def designOfRepositories: Design =
    newDesign
      .bind[AccountRepository[Effect]].to[AccountRepositoryOnRDB]

  private[adapters] def designOfServices: Design =
    newDesign
      .bind[EncryptService[Effect]].to[EncryptServiceOnBCrypt]

  private[adapters] def designOfHttpPresenters: Design =
    newDesign
      .bind[CreateAccountPresenter].toEagerSingleton

  private[adapters] def designOfHttpControllers: Design =
    newDesign
      .bind[Controller].toEagerSingleton

  def design(environment: AppType): Design =
    designOfRuntime(environment)
      .add(designOfRepositories)
      .add(designOfServices)
      .add(designOfHttpControllers)
      .add(designOfHttpPresenters)

}

object DISettings extends DISettings
