package adapters.http.presenters

import adapters.http.json.CreateAccountResponseJson
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import usecases.UseCaseApplicationError
import usecases.account.AccountCreateOutput

trait CreateAccountPresenter extends Presenter[AccountCreateOutput] {

  override protected def convert(outputData: AccountCreateOutput): Route =
    complete(CreateAccountResponseJson(Some(outputData.id.value)))

  override protected def convert(useCaseApplicationError: UseCaseApplicationError): Route =
    complete(
      HttpResponse(
        StatusCodes.BadRequest,
        entity = HttpEntity(contentType = ContentTypes.`application/json`, string = useCaseApplicationError.message)
      )
    )
}
