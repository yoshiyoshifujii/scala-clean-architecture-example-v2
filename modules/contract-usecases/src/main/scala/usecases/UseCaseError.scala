package usecases

import cats.data.NonEmptyList
import domain.DomainError

sealed trait UseCaseError

case class UseCaseSystemError(cause: Throwable)     extends UseCaseError

case class UseCaseApplicationError(message: String) extends UseCaseError
object UseCaseApplicationError {
  def apply(message: NonEmptyList[DomainError]): UseCaseApplicationError =
    new UseCaseApplicationError(message.toString())
}
