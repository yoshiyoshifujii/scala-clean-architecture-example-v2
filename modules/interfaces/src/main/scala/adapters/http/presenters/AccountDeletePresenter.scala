package adapters.http.presenters

import adapters.http.json.AccountDeleteResponseJson
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import usecases.UseCaseApplicationError
import usecases.signed.AccountDeleteOutput

trait AccountDeletePresenter extends Presenter[AccountDeleteOutput] {

  override protected def convert(outputData: AccountDeleteOutput): Route =
    complete(AccountDeleteResponseJson())

  override protected def convert(useCaseApplicationError: UseCaseApplicationError): Route =
    complete(
      HttpResponse(
        StatusCodes.BadRequest,
        entity = HttpEntity(contentType = ContentTypes.`application/json`, string = useCaseApplicationError.message)
      )
    )
}
