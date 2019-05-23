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
      override def generate: Id[AccountId] = AccountId("2")
    }
    val accountRepository: AccountRepository[Id] = new AccountRepository[Id] {
      override def findBy(email: Email): Id[Option[ResolvedAccount]] = email.value.value match {
        case "a@a.com" => Some(account)
        case _ => None
      }
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

    "success - already exists" in {
      assert(
        useCase.execute(AccountCreateInput(email = "a@a.com", password = "hogeHoge123", name = "hoge hogeo")).id === "1"
      )
    }

    "success - new" in {
      assert(
        useCase.execute(AccountCreateInput(email = "b@b.com", password = "fugaFuga123", name = "fuga fugao")).id === "2"
      )
    }

    "fail - bad email address" in {
      assert(
        intercept[Exception](
          useCase.execute(AccountCreateInput(email = "", password = "hogeHoge123", name = "hoge hogeo"))
        ).getMessage === "UseCaseApplicationError(Predicate taking size() = 0 failed: Left predicate of (!(0 < 1) && !(0 > 100)) failed: Predicate (0 < 1) did not fail.)"
      )
      assert(
        intercept[Exception](
          useCase.execute(
            AccountCreateInput(
              email =
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
              password = "hogeHoge123",
              name = "hoge hogeo"
            )
          )
        ).getMessage === "UseCaseApplicationError(Predicate taking size(aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa) = 101 failed: Right predicate of (!(101 < 1) && !(101 > 100)) failed: Predicate (101 > 100) did not fail.)"
      )
    }

    "fail - bad password" in {
      assert(
        intercept[Exception](
          useCase.execute(AccountCreateInput(email = "a@a.com", password = "", name = "hoge hogeo"))
        ).getMessage === """UseCaseApplicationError(Predicate failed: "".matches("[0-9a-zA-Z]{8,48}").)"""
      )
      assert(
        intercept[Exception](
          useCase.execute(AccountCreateInput(email = "a@a.com", password = "hoge", name = "hoge hogeo"))
        ).getMessage === """UseCaseApplicationError(Predicate failed: "hoge".matches("[0-9a-zA-Z]{8,48}").)"""
      )
    }
  }

}
