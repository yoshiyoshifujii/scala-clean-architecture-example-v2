package adapters.http.presenters

import adapters.{ AppType, Effect }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import scalaz.zio.ZIO
import usecases.{ UseCaseApplicationError, UseCaseSystemError }

trait Presenter[OutputData] {

  def response(res: Effect[OutputData])(implicit runtime: scalaz.zio.Runtime[AppType]): Route = {
    val future = runtime.unsafeRunToFuture {
      res.foldM(
        {
          case appError: UseCaseApplicationError => ZIO.succeed(convert(appError))
          case UseCaseSystemError(cause)         => ZIO.fail(cause)
        },
        success => ZIO.succeed(convert(success))
      )
    }
    onSuccess(future)(identity)
  }

  protected def convert(outputData: OutputData): Route
  protected def convert(useCaseApplicationError: UseCaseApplicationError): Route

}
