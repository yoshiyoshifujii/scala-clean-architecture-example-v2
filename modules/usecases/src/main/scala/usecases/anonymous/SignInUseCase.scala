package usecases.anonymous

import cats.implicits._
import domain.account.PlainPassword
import domain.common.Email
import repositories.AccountRepository
import services.{ EncryptService, TokenService }
import usecases.{ UseCase, UseCaseApplicationError, UseCaseMonadError }

case class SignInInput(email: Email, password: PlainPassword)
case class SignInOutput(token: String)

class SignInUseCase[F[_]](
    accountRepository: AccountRepository[F],
    encryptService: EncryptService[F],
    tokenService: TokenService[F]
) extends UseCase[F, SignInInput, SignInOutput] {

  override def execute(inputData: SignInInput)(implicit ME: UseCaseMonadError[F]): F[SignInOutput] =
    for {
      maybe   <- accountRepository.findBy(inputData.email)
      account <- maybe.map(ME.pure).getOrElse(ME.raiseError(UseCaseApplicationError("missed.")))
      matched <- encryptService.matches(inputData.password.value.value, account.password.value)
      token <- if (matched) tokenService.generate(account.id)
      else ME.raiseError[String](UseCaseApplicationError("missed."))
    } yield SignInOutput(token)

}
