package adapters.http.presenters

import adapters.http.json.{ AccountGetsResponseJson, AccountJson }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import usecases.signed.AccountGetsOutput

trait AccountGetsPresenter extends Presenter[AccountGetsOutput] {

  override protected def convert(outputData: AccountGetsOutput): Route =
    complete(
      AccountGetsResponseJson(
        outputData.accounts.map { account =>
          AccountJson(account.id.value, account.email.value.value, account.name.value.value)
        }
      )
    )

}
