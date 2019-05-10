import entities.EntitiesValidationResult
import scalaz.zio.ZIO

package object usecases {

  type UseCaseZIOR[R, A] = ZIO[R, UseCaseError, A]
  type UseCaseZIO[A]     = UseCaseZIOR[Any, A]

  implicit class EntitiesError2UseCaseZIO[A](val v: EntitiesValidationResult[A]) extends AnyVal {
    def toUseCaseZIOR[R]: UseCaseZIOR[R, A] =
      v.fold(
        ne => ZIO.fail(UseCaseApplicationError(ne)),
        ZIO.succeed
      )

    def toUseCaseZIO: UseCaseZIO[A] = toUseCaseZIOR[Any]
  }

}
