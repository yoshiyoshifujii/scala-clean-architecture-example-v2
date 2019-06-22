package adapters.http.presenters

import adapters.http.json.{ AccountGetResponseJson, AccountJson }
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import usecases.UseCaseApplicationError
import usecases.signed.AccountGetOutput

trait AccountGetPresenter extends Presenter[AccountGetOutput] {

  override protected def convert(outputData: AccountGetOutput): Route =
    complete(
      AccountGetResponseJson(
        Some(AccountJson(outputData.id.value, outputData.email.value.value, outputData.name.value.value))
      )
    )

  override protected def convert(useCaseApplicationError: UseCaseApplicationError): Route =
    complete(
      HttpResponse(
        StatusCodes.BadRequest,
        entity = HttpEntity(contentType = ContentTypes.`application/json`, string = useCaseApplicationError.message)
      )
    )
}
