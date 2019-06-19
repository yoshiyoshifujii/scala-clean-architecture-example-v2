package usecases.signed

import adapters.gateway.repositories.memory.id.AccountRepositoryByMemoryWithId
import cats.Id
import com.github.j5ik2o.dddbase.AggregateNotFoundException
import domain.account.{ Account, AccountId, AccountName, EncryptedPassword }
import domain.common.Email
import org.scalatest.{ DiagrammedAssertions, FreeSpec }
import repositories.AccountRepository

class DeleteAccountUseCaseSpec extends FreeSpec with DiagrammedAssertions {

  "DeleteAccountUseCase" - {
    val accountRepository: AccountRepository[Id] = new AccountRepositoryByMemoryWithId()
    val useCase: DeleteAccountUseCase[Id]        = new DeleteAccountUseCase(accountRepository)

    val accountId   = AccountId()
    val email       = Email.generate("a@a.com")
    val accountName = AccountName.generate("hoge hogeo")
    val account     = Account.generate(accountId, email, accountName, EncryptedPassword("xxx"))

    import usecases.SampleInterfacesLayer.SampleErrors._
    "execute" in {
      assert(accountRepository.store(account) === 1L)

      val result = useCase.execute(AccountDeleteInput(accountId))

      assert(result.id === accountId)
      assertThrows[AggregateNotFoundException](accountRepository.resolveById(accountId))
    }
  }

}
