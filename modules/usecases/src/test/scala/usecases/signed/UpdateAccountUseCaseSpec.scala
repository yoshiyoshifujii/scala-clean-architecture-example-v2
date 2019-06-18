package usecases.signed

import adapters.gateway.repositories.memory.id.AccountRepositoryByMemoryWithId
import cats.Id
import domain.account.{ Account, AccountId, AccountName, EncryptedPassword }
import domain.common.Email
import org.scalatest.{ DiagrammedAssertions, FreeSpec }
import repositories.AccountRepository

class UpdateAccountUseCaseSpec extends FreeSpec with DiagrammedAssertions {

  "UpdateAccountUseCase" - {
    val accountRepository: AccountRepository[Id] = new AccountRepositoryByMemoryWithId()
    val useCase: UpdateAccountUseCase[Id]        = new UpdateAccountUseCase(accountRepository)

    val accountId   = AccountId()
    val email       = Email.generate("a@a.com")
    val accountName = AccountName.generate("hoge hogeo")
    val account     = Account.generate(accountId, email, accountName, EncryptedPassword("xxx"))

    "execute" in {
      import usecases.SampleInterfacesLayer.SampleErrors._
      assert(accountRepository.store(account) === 1L)

      val newAccountName = AccountName.generate("fuga fugao")
      val result         = useCase.execute(AccountUpdateInput(accountId, newAccountName))
      assert(result.id === accountId)
      assert(accountRepository.resolveById(accountId).name === newAccountName)
    }
  }

}
