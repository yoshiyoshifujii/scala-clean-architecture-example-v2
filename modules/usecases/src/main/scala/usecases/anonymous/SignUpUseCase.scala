package usecases.anonymous

import cats.implicits._
import domain.account._
import domain.common.Email
import repositories.AccountRepository
import services.EncryptService
import usecases.{ UseCase, UseCaseMonadError }

case class SignUpInput(email: Email, password: PlainPassword, name: AccountName)
case class SignUpOutput(id: AccountId)

class SignUpUseCase[F[_]](
    accountRepository: AccountRepository[F],
    encryptService: EncryptService[F]
) extends UseCase[F, SignUpInput, SignUpOutput] {

  override def execute(inputData: SignUpInput)(implicit ME: UseCaseMonadError[F]): F[SignUpOutput] =
    for {
      encrypted <- encryptService.encrypt(inputData.password.value.value)
      generated = Account.generate(AccountId(), inputData.email, inputData.name, EncryptedPassword(encrypted))
      _ <- accountRepository.store(generated)
    } yield SignUpOutput(generated.id)

}
