package usecases

import cats.data.NonEmptyList
import entities.EntitiesError

sealed trait UseCaseError

case class UseCaseSystemError(cause: Throwable)     extends UseCaseError

case class UseCaseApplicationError(message: String) extends UseCaseError
object UseCaseApplicationError {
  def apply(message: NonEmptyList[EntitiesError]): UseCaseApplicationError =
    new UseCaseApplicationError(message.toString())
}
