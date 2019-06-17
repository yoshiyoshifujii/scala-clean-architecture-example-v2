package usecases.account

import cats.implicits._
import domain.account._
import domain.common.Email
import repositories.AccountRepository
import services.EncryptService
import usecases.{ UseCase, UseCaseMonadError }

case class AccountCreateInput(email: Email, password: PlainPassword, name: AccountName)
case class AccountCreateOutput(id: AccountId)

class CreateAccountUseCase[F[_]](
    accountRepository: AccountRepository[F],
    encryptService: EncryptService[F]
) extends UseCase[F, AccountCreateInput, AccountCreateOutput] {

  override def execute(inputData: AccountCreateInput)(implicit ME: UseCaseMonadError[F]): F[AccountCreateOutput] =
    for {
      encrypted <- encryptService.encrypt(inputData.password.value.value)
      generated = Account.generate(AccountId(), inputData.email, inputData.name, EncryptedPassword(encrypted))
      _ <- accountRepository.store(generated)
    } yield AccountCreateOutput(generated.id)

}
