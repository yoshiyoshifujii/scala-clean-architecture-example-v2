package usecases.signed

import adapters.gateway.repositories.memory.id.AccountRepositoryByMemoryWithId
import cats.Id
import domain.account.{ Account, AccountId, AccountName, Auth, EncryptedPassword }
import domain.common.Email
import org.scalatest.{ DiagrammedAssertions, FreeSpec }
import repositories.AccountRepository

class AccountGetsUseCaseSpec extends FreeSpec with DiagrammedAssertions {

  "AccountGetsUseCase" - {
    val accountRepository: AccountRepository[Id] = new AccountRepositoryByMemoryWithId()
    val useCase: AccountGetsUseCase[Id]          = new AccountGetsUseCase(accountRepository)

    import usecases.SampleInterfacesLayer.SampleErrors._
    "execute" in {
      assert(useCase.execute(AccountGetsInput(Auth(AccountId()))).accounts.isEmpty)

      assert(
        accountRepository.store(
          Account
            .generate(AccountId(), Email.generate("a0@a.com"), AccountName.generate("a0"), EncryptedPassword("xxx"))
        ) === 1L
      )

      assert(useCase.execute(AccountGetsInput(Auth(AccountId()))).accounts.length === 1)

      assert(
        accountRepository.store(
          Account
            .generate(AccountId(), Email.generate("a1@a.com"), AccountName.generate("a1"), EncryptedPassword("xxx"))
        ) === 1L
      )

      assert(useCase.execute(AccountGetsInput(Auth(AccountId()))).accounts.length === 2)
    }

  }

}
