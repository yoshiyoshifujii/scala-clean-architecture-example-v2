package usecases

import cats.MonadError
import domain.DomainValidationResult

package object usecases {

  type UseCaseMonadError[F[_]] = MonadError[F, UseCaseError]

  implicit class DomainError2MonadError[A](val v: DomainValidationResult[A]) extends AnyVal {
    def toM[F[_]](implicit ME: UseCaseMonadError[F]): F[A] =
      v.fold(
        ne => ME.raiseError(UseCaseApplicationError(ne)),
        ME.pure
      )
  }

}
