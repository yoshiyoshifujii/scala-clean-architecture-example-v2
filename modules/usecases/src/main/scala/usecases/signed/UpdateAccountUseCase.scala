package usecases.signed

import domain.account.{ Account, AccountId, AccountName, ResolvedAccount }
import repositories.AccountRepository
import usecases.{ UseCase, UseCaseMonadError }
import cats.implicits._

case class AccountUpdateInput(accountId: AccountId, name: AccountName)
case class AccountUpdateOutput(id: AccountId)

class UpdateAccountUseCase[F[_]](
    accountRepository: AccountRepository[F]
) extends UseCase[F, AccountUpdateInput, AccountUpdateOutput] {
  override def execute(inputData: AccountUpdateInput)(implicit ME: UseCaseMonadError[F]): F[AccountUpdateOutput] =
    for {
      account <- accountRepository.resolveById(inputData.accountId)
      renamedAccount = Account.rename(account.asInstanceOf[ResolvedAccount], inputData.name)
      _ <- accountRepository.store(renamedAccount)
    } yield AccountUpdateOutput(renamedAccount.id)
}
