package usecases.signed

import adapters.gateway.repositories.memory.id.AccountRepositoryByMemoryWithId
import cats.Id
import domain.account.{ Account, AccountId, AccountName, Auth, EncryptedPassword }
import domain.common.Email
import org.scalatest.{ DiagrammedAssertions, FreeSpec }
import repositories.AccountRepository

class AccountUpdateUseCaseSpec extends FreeSpec with DiagrammedAssertions {

  "AccountUpdateUseCase" - {
    val accountRepository: AccountRepository[Id] = new AccountRepositoryByMemoryWithId()
    val useCase: AccountUpdateUseCase[Id]        = new AccountUpdateUseCase(accountRepository)

    val accountId   = AccountId()
    val email       = Email.generate("a@a.com")
    val accountName = AccountName.generate("hoge hogeo")
    val account     = Account.generate(accountId, email, accountName, EncryptedPassword("xxx"))
    val auth        = Auth(accountId)

    import usecases.SampleInterfacesLayer.SampleErrors._
    "execute" in {
      assert(accountRepository.store(account) === 1L)

      val newAccountName = AccountName.generate("fuga fugao")
      val result         = useCase.execute(AccountUpdateInput(auth, accountId, newAccountName))
      assert(result.id === accountId)
      assert(accountRepository.resolveById(accountId).name === newAccountName)

      assertThrows[Exception](useCase.execute(AccountUpdateInput(auth, AccountId(), newAccountName)))
    }
  }

}
