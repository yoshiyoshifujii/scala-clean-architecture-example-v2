package adapters.http.presenters

import adapters.http.json.CreateAccountResponseJson
import usecases.account.AccountCreateOutput

trait CreateAccountPresenter[F[_]] extends Presenter[F, AccountCreateOutput, CreateAccountResponseJson] {

  override protected def response(outputData: AccountCreateOutput): CreateAccountResponseJson =
    CreateAccountResponseJson(outputData.id)

}
