package adapters.http.presenters

import adapters.{ AppType, Effect }
import scalaz.zio.ZIO
import usecases.{ UseCaseApplicationError, UseCaseSystemError }

trait Presenter[OutputData, ViewModel] {

  def response(res: Effect[OutputData]): ZIO[AppType, Throwable, ViewModel] =
    res.foldM(
      {
        case appError: UseCaseApplicationError => ZIO.succeed(convert(appError))
        case UseCaseSystemError(cause)         => ZIO.fail(cause)
      },
      success => ZIO.succeed(convert(success))
    )

  protected def convert(outputData: OutputData): ViewModel
  protected def convert(useCaseApplicationError: UseCaseApplicationError): ViewModel

}
