package usecases.signed

import adapters.gateway.repositories.memory.id.AccountRepositoryByMemoryWithId
import cats.Id
import domain.account.{ Account, AccountId, AccountName, Auth, EncryptedPassword }
import domain.common.Email
import org.scalatest.{ DiagrammedAssertions, FreeSpec }
import repositories.AccountRepository

class AccountGetUseCaseSpec extends FreeSpec with DiagrammedAssertions {

  "AccountGetUseCase" - {
    val accountRepository: AccountRepository[Id] = new AccountRepositoryByMemoryWithId()
    val useCase: AccountGetUseCase[Id]           = new AccountGetUseCase(accountRepository)

    val accountId   = AccountId()
    val email       = Email.generate("a0@a.com")
    val accountName = AccountName.generate("a0")

    import usecases.SampleInterfacesLayer.SampleErrors._
    "execute" in {
      assertThrows[Exception](useCase.execute(AccountGetInput(Auth(accountId), accountId)))

      assert(
        accountRepository.store(
          Account
            .generate(accountId, email, accountName, EncryptedPassword("xxx"))
        ) === 1L
      )

      val result = useCase.execute(AccountGetInput(Auth(accountId), accountId))
      assert(result.id === accountId)
      assert(result.email === email)
      assert(result.name === accountName)
    }

  }

}
