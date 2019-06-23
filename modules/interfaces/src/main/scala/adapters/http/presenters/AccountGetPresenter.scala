package adapters.http.presenters

import adapters.http.json.{ AccountGetResponseJson, AccountJson }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import usecases.signed.AccountGetOutput

trait AccountGetPresenter extends Presenter[AccountGetOutput] {

  override protected def convert(outputData: AccountGetOutput): Route =
    complete(
      AccountGetResponseJson(
        Some(AccountJson(outputData.id.value, outputData.email.value.value, outputData.name.value.value))
      )
    )

}
