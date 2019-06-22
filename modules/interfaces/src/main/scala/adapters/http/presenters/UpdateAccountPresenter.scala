package adapters.http.presenters

import adapters.http.json.AccountUpdateResponseJson
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import usecases.UseCaseApplicationError
import usecases.signed.AccountUpdateOutput

trait UpdateAccountPresenter extends Presenter[AccountUpdateOutput] {

  override protected def convert(outputData: AccountUpdateOutput): Route =
    complete(AccountUpdateResponseJson(Some(outputData.id.value)))

  override protected def convert(useCaseApplicationError: UseCaseApplicationError): Route =
    complete(
      HttpResponse(
        StatusCodes.BadRequest,
        entity = HttpEntity(contentType = ContentTypes.`application/json`, string = useCaseApplicationError.message)
      )
    )
}
