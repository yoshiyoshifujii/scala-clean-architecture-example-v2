import cats.MonadError
import domain.{ DomainError, DomainValidationResult }

package object usecases {

  type UseCaseMonadError[F[_]] = MonadError[F, UseCaseError]

  implicit class DomainError2MonadError[A](val v: DomainValidationResult[A]) extends AnyVal {
    def toF[F[_]](implicit ME: UseCaseMonadError[F]): F[A] =
      v.fold(
        ne => ME.raiseError(UseCaseApplicationError(ne)),
        ME.pure
      )
  }

  implicit class EitherDomainError2MonadError[A](val v: Either[DomainError, A]) extends AnyVal {
    def toF[F[_]](implicit ME: UseCaseMonadError[F]): F[A] =
      v.fold(
        de => ME.raiseError(UseCaseApplicationError(de.message)),
        ME.pure
      )
  }

}
