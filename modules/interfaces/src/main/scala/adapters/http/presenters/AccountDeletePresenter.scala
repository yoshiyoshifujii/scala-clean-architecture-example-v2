package adapters.http.presenters

import adapters.http.json.AccountDeleteResponseJson
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import usecases.signed.AccountDeleteOutput

trait AccountDeletePresenter extends Presenter[AccountDeleteOutput] {

  override protected def convert(outputData: AccountDeleteOutput): Route =
    complete(AccountDeleteResponseJson())

}
