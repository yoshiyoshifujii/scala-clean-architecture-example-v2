package adapters.http.presenters

import adapters.http.json.CreateAccountResponseJson
import usecases.UseCaseApplicationError
import usecases.account.AccountCreateOutput

trait CreateAccountPresenter extends Presenter[AccountCreateOutput, CreateAccountResponseJson] {

  override protected def response(outputData: AccountCreateOutput): CreateAccountResponseJson =
    CreateAccountResponseJson(Some(outputData.id.value))

  override protected def response(useCaseApplicationError: UseCaseApplicationError): CreateAccountResponseJson =
    CreateAccountResponseJson(None, error_messages = Seq(useCaseApplicationError.message))
}
