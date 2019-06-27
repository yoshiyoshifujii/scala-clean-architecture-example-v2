package adapters.gateway.services

import adapters.{ AppType, Effect }
import com.github.t3hnar.bcrypt._
import zio.ZIO
import services.EncryptService
import usecases.UseCaseSystemError

import scala.util.Try

trait EncryptServiceByBCrypt extends EncryptService[Effect] {

  override def encrypt(value: String): Effect[String] =
    ZIO
      .accessM[AppType] { _ =>
        ZIO
          .fromTry {
            for {
              salt     <- Try(generateSalt)
              bcrypted <- value.bcryptSafe(salt)
            } yield bcrypted
          }
      }.foldM(
        cause => ZIO.fail(UseCaseSystemError(cause)),
        ZIO.succeed
      )

  override def matches(value0: String, value1: String): Effect[Boolean] =
    ZIO
      .accessM[AppType] { _ =>
        ZIO
          .fromTry {
            value0.isBcryptedSafe(value1)
          }
      }.foldM(
        cause => ZIO.fail(UseCaseSystemError(cause)),
        ZIO.succeed
      )
}
