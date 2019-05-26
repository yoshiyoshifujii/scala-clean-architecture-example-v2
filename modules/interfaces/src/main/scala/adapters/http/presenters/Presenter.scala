package adapters.http.presenters

import adapters.{ AppType, Effect }
import scalaz.zio.ZIO
import usecases.{ UseCaseApplicationError, UseCaseSystemError }

trait Presenter[OutputData, ViewModel] {

  def response(res: Effect[OutputData]): ZIO[AppType, Nothing, ViewModel] =
    res.fold(
      {
        case appError: UseCaseApplicationError => response(appError)
        case UseCaseSystemError(cause)         => throw cause
      },
      response
    )

  protected def response(outputData: OutputData): ViewModel
  protected def response(useCaseApplicationError: UseCaseApplicationError): ViewModel

}
