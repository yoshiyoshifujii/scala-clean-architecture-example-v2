package usecases.account

import cats.implicits._
import domain.account.{ Account, AccountId, AccountName, EncryptedPassword, PlainPassword }
import domain.common.Email
import gateway.generators.AccountIdGenerator
import gateway.repositories.AccountRepository
import gateway.services.EncryptService
import usecases.{ UseCase, UseCaseMonadError, _ }

case class AccountCreateInput(email: Email, password: PlainPassword, name: AccountName)
case class AccountCreateOutput(id: AccountId)

class CreateAccountUseCase[F[_]](
    accountIdGenerator: AccountIdGenerator[F],
    accountRepository: AccountRepository[F],
    encryptService: EncryptService[F]
) extends UseCase[F, AccountCreateInput, AccountCreateOutput] {

  override def execute(inputData: AccountCreateInput)(implicit ME: UseCaseMonadError[F]): F[AccountCreateOutput] =
    for {
      maybe <- accountRepository.findBy(inputData.email)
      stored <- maybe match {
        case Some(account) => ME.pure(account)
        case None =>
          for {
            id        <- accountIdGenerator.generate
            encrypted <- encryptService.encrypt(inputData.password.value.value)
            _ <- accountRepository.store(
              Account.generate(id, inputData.email, inputData.name, EncryptedPassword(encrypted))
            )
          } yield Account.generateResolved(id, inputData.email, inputData.name, EncryptedPassword(encrypted))
      }
    } yield AccountCreateOutput(stored.id)

}
