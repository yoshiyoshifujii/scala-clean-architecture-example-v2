package adapters.errors

import adapters.Effect
import cats._
import zio.ZIO
import usecases.UseCaseError

object Errors {
  implicit val useCaseMonadErrorForZIO: MonadError[Effect, UseCaseError] =
    new MonadError[Effect, UseCaseError] with StackSafeMonad[Effect] {
      override def pure[A](x: A): Effect[A]                                                   = ZIO.succeed(x)
      override def flatMap[A, B](fa: Effect[A])(f: A => Effect[B]): Effect[B]                 = fa.flatMap(f)
      override def raiseError[A](e: UseCaseError): Effect[A]                                  = ZIO.fail(e)
      override def handleErrorWith[A](fa: Effect[A])(f: UseCaseError => Effect[A]): Effect[A] = fa.catchAll(f)
    }

}
