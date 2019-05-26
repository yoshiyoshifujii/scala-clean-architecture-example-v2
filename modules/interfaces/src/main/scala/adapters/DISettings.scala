package adapters

import adapters.http.controllers.Controller
import adapters.http.presenters.CreateAccountPresenter
import wvlet.airframe._

trait DISettings {

  private[adapters] def designOfHttpControllers: Design =
    newDesign
      .bind[Controller[Effect]].toEagerSingleton

  private[adapters] def designOfHttpPresenters: Design =
    newDesign
      .bind[CreateAccountPresenter[Effect]].toEagerSingleton

  def design: Design =
    designOfHttpControllers
      .add(designOfHttpPresenters)

}

object DISettings extends DISettings
