package usecases.account

import cats.Id
import domain.account._
import domain.common.Email
import repositories.AccountRepository
import infrastructure.ulid.ULID
import org.scalatest.FreeSpec
import services.EncryptService

class CreateAccountUseCaseSpec extends FreeSpec {

  "CreateAccountUseCase" - {
    val email1         = Email.generate("a@a.com")
    val email2         = Email.generate("b@b.com")
    val accountName1   = AccountName.generate("hoge hogeo")
    val accountName2   = AccountName.generate("fuga fugao")
    val plainPassword1 = PlainPassword.generate("hogeHoge123")
    val plainPassword2 = PlainPassword.generate("fugaFuga123")

    val ulid1      = ULID()
    val accountId1 = AccountId(ulid1)

    val account = Account.generateResolved(
      accountId1,
      email1,
      accountName1,
      EncryptedPassword("xxx")
    )

    val accountRepository: AccountRepository[Id] = new AccountRepository[Id] {
      override def findBy(email: Email): Id[Option[ResolvedAccount]] = email.value.value match {
        case "a@a.com" => Some(account)
        case _         => None
      }
      override def resolveById(id: AccountId): Id[Account] = account
      override def store(aggregate: Account): Id[Long]     = 1L
    }
    val encryptService: EncryptService[Id] = new EncryptService[Id] {
      override def encrypt(value: String): Id[String]                   = "xxx"
      override def matches(value0: String, value1: String): Id[Boolean] = ???
    }
    val useCase: CreateAccountUseCase[Id] = new CreateAccountUseCase[Id](
      accountRepository,
      encryptService
    )
    import usecases.SampleInterfacesLayer.SampleErrors._

    "success - already exists" in {
      assert(
        intercept[Exception](
          useCase.execute(AccountCreateInput(email = email1, password = plainPassword1, name = accountName1))
        ).getMessage === "UseCaseApplicationError(already exists.)"
      )
    }

    "success - new" in {
      assert(
        useCase
          .execute(AccountCreateInput(email = email2, password = plainPassword2, name = accountName2)).id.breachEncapsulationOfValue !== ulid1
      )
    }

  }

}
