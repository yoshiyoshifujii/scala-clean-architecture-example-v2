package usecases.account

import cats.Id
import domain.account.{ Account, AccountId, AccountName, EncryptedPassword, ResolvedAccount }
import domain.common.Email
import gateway.generators.AccountIdGenerator
import gateway.repositories.AccountRepository
import gateway.services.EncryptService
import org.scalatest.FreeSpec

class AccountCreateUseCaseSpec extends FreeSpec {

  "AccountCreateUseCase" - {
    val account = Account.generateResolved(
      AccountId("1"),
      Email.generate("a@a.com").right.get,
      AccountName.generate("hoge hogeo").right.get,
      EncryptedPassword("xxx")
    )

    val accountIdGenerator: AccountIdGenerator[Id] = new AccountIdGenerator[Id] {
      override def generate: Id[AccountId] = account.id
    }
    val accountRepository: AccountRepository[Id] = new AccountRepository[Id] {
      override def findBy(email: Email): Id[Option[ResolvedAccount]] = Some(account)
      override def resolveById(id: AccountId): Id[Account]           = account
      override def store(aggregate: Account): Id[Long]               = 1L
    }
    val encryptService: EncryptService[Id] = new EncryptService[Id] {
      override def encrypt(value: String): Id[String]                   = "xxx"
      override def matches(value0: String, value1: String): Id[Boolean] = ???
    }
    val useCase: AccountCreateUseCase[Id] = new AccountCreateUseCase[Id](
      accountIdGenerator,
      accountRepository,
      encryptService
    )
    import usecases.SampleInterfacesLayer.SampleErrors._

    "success" in {
      assert(
        useCase.execute(AccountCreateInput(email = "a@a.com", password = "hogeHoge123", name = "hoge hogeo")).id === "1"
      )
    }

    "fail - bad email address" in {
      assertThrows[Exception](
        useCase.execute(AccountCreateInput(email = "", password = "hogeHoge123", name = "hoge hogeo"))
      )
      assertThrows[Exception](
        useCase.execute(
          AccountCreateInput(
            email =
              "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            password = "hogeHoge123",
            name = "hoge hogeo"
          )
        )
      )
    }
  }

}
