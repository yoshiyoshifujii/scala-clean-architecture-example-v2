package usecases.signed

import cats.implicits._
import domain.account.{ AccountId, AccountName, Auth }
import domain.common.Email
import repositories.AccountRepository
import usecases.{ UseCase, UseCaseMonadError }

case class AccountGetInput(auth: Auth, accountId: AccountId)
case class AccountGetOutput(id: AccountId, email: Email, name: AccountName)

class AccountGetUseCase[F[_]](
    accountRepository: AccountRepository[F]
) extends UseCase[F, AccountGetInput, AccountGetOutput] {

  override def execute(inputData: AccountGetInput)(implicit ME: UseCaseMonadError[F]): F[AccountGetOutput] =
    for {
      account <- accountRepository.resolveById(inputData.accountId)
    } yield AccountGetOutput(account.id, account.email, account.name)

}
