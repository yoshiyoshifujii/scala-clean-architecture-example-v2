package adapters.http.presenters

import cats.implicits._
import usecases.UseCaseMonadError

trait Presenter[F[_], OutputData, ViewModel] {

  def response(res: F[OutputData])(implicit ME: UseCaseMonadError[F]): F[ViewModel] =
    res.map((outputData: OutputData) => response(outputData))

  protected def response(outputData: OutputData): ViewModel

}
