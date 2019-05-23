package adapters.http.controllers

import adapters.http.presenters.CreateAccountPresenter
import usecases.account.CreateAccountUseCase
import wvlet.airframe._

trait Controller[F[_]] {

  private val createAccountUseCase: CreateAccountUseCase[F] = bind[CreateAccountUseCase[F]]
  private val createAccountPresenter: CreateAccountPresenter[F] = bind[CreateAccountPresenter[F]]


}
