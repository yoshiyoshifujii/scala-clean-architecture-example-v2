package usecases.signed

import cats.implicits._
import domain.account.AccountId
import repositories.AccountRepository
import usecases.{ UseCase, UseCaseMonadError }

case class AccountDeleteInput(accountId: AccountId)
case class AccountDeleteOutput(id: AccountId)

class DeleteAccountUseCase[F[_]](
    accountRepository: AccountRepository[F]
) extends UseCase[F, AccountDeleteInput, AccountDeleteOutput] {

  override def execute(inputData: AccountDeleteInput)(implicit ME: UseCaseMonadError[F]): F[AccountDeleteOutput] =
    for {
      _ <- accountRepository.hardDelete(inputData.accountId)
    } yield AccountDeleteOutput(inputData.accountId)

}
