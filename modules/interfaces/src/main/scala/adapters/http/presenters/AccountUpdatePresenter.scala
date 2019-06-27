package adapters.http.presenters

import adapters.http.json.AccountUpdateResponseJson
import akka.http.scaladsl.model.{ headers, ContentTypes, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.j5ik2o.dddbase.AggregateNotFoundException
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import zio.Task
import usecases.signed.AccountUpdateOutput

trait AccountUpdatePresenter extends Presenter[AccountUpdateOutput] {

  override protected def convert(outputData: AccountUpdateOutput): Route =
    complete(AccountUpdateResponseJson(Some(outputData.id.value)))

  override protected def handleError(cause: Throwable): Task[Route] =
    cause match {
      case _: AggregateNotFoundException =>
        Task.succeed(
          complete(
            HttpResponse(
              StatusCodes.NotFound
            ).addHeader(headers.`Content-Type`(ContentTypes.`application/json`))
          )
        )
      case _ => Task.fail(cause)
    }
}
