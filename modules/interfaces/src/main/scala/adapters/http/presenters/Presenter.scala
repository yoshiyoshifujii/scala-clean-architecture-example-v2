package adapters.http.presenters

import adapters.{ AppType, Effect }
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import zio.{ Task, ZIO }
import usecases.{ UseCaseApplicationError, UseCaseError, UseCaseSystemError }

trait Presenter[OutputData] {

  def response(res: Effect[OutputData])(implicit runtime: zio.Runtime[AppType]): Route = {
    val future = runtime.unsafeRunToFuture {
      res.foldM(
        handleError,
        success => ZIO.succeed(convert(success))
      )
    }
    onSuccess(future)(identity)
  }

  protected def convert(outputData: OutputData): Route

  protected def handleError(useCaseError: UseCaseError): Task[Route] =
    useCaseError match {
      case appError: UseCaseApplicationError => handleError(appError)
      case UseCaseSystemError(cause)         => handleError(cause)
    }

  protected def handleError(useCaseApplicationError: UseCaseApplicationError): Task[Route] =
    Task.succeed {
      complete(
        HttpResponse(
          StatusCodes.BadRequest,
          entity = HttpEntity(contentType = ContentTypes.`application/json`, string = useCaseApplicationError.message)
        )
      )
    }

  protected def handleError(cause: Throwable): Task[Route] = Task.fail(cause)

}
