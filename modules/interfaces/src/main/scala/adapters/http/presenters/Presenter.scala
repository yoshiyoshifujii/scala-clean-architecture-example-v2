package adapters.http.presenters

import adapters.{ AppType, Effect }
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import scalaz.zio.{ Task, ZIO }
import usecases.{ UseCaseApplicationError, UseCaseError, UseCaseSystemError }

trait Presenter[OutputData] {

  def response(res: Effect[OutputData])(implicit runtime: scalaz.zio.Runtime[AppType]): Route = {
    val future = runtime.unsafeRunToFuture {
      res.foldM(
        convert,
        success => ZIO.succeed(convert(success))
      )
    }
    onSuccess(future)(identity)
  }

  protected def convert(outputData: OutputData): Route

  protected def convert(useCaseApplicationError: UseCaseApplicationError): Task[Route] =
    Task.succeed {
      complete(
        HttpResponse(
          StatusCodes.BadRequest,
          entity = HttpEntity(contentType = ContentTypes.`application/json`, string = useCaseApplicationError.message)
        )
      )
    }

  protected def convert(cause: Throwable): Task[Route] = Task.fail(cause)

  protected def convert(useCaseError: UseCaseError): Task[Route] =
    useCaseError match {
      case appError: UseCaseApplicationError => convert(appError)
      case UseCaseSystemError(cause)         => convert(cause)
    }

}
