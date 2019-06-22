package usecases.signed

import cats.implicits._
import domain.account.{ AccountId, AccountName, Auth }
import domain.common.Email
import repositories.AccountRepository
import usecases.{ UseCase, UseCaseMonadError }

case class AccountGetsInput(auth: Auth)
case class AccountOutput(id: AccountId, email: Email, name: AccountName)
case class AccountGetsOutput(accounts: Seq[AccountOutput])

class AccountGetsUseCase[F[_]](
    accountRepository: AccountRepository[F]
) extends UseCase[F, AccountGetsInput, AccountGetsOutput] {

  override def execute(inputData: AccountGetsInput)(implicit ME: UseCaseMonadError[F]): F[AccountGetsOutput] =
    for {
      accounts <- accountRepository.resolveAll
    } yield AccountGetsOutput(
      accounts.map { account =>
        AccountOutput(account.id, account.email, account.name)
      }
    )

}
