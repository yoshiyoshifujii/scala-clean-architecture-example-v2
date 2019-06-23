package adapters.http.presenters

import adapters.http.json.AccountUpdateResponseJson
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import usecases.signed.AccountUpdateOutput

trait AccountUpdatePresenter extends Presenter[AccountUpdateOutput] {

  override protected def convert(outputData: AccountUpdateOutput): Route =
    complete(AccountUpdateResponseJson(Some(outputData.id.value)))

}
