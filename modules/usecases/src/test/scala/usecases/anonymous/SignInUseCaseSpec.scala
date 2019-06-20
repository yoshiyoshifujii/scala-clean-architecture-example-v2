package usecases.anonymous

import adapters.gateway.repositories.memory.id.AccountRepositoryByMemoryWithId
import cats.Id
import domain.account._
import domain.common.Email
import org.scalatest.{ DiagrammedAssertions, FreeSpec }
import repositories.AccountRepository
import services.{ EncryptService, TokenService }

class SignInUseCaseSpec extends FreeSpec with DiagrammedAssertions {

  "SignInUseCase" - {
    val accuntId      = AccountId()
    val email         = Email.generate("a@a.com")
    val accountName   = AccountName.generate("hoge hogeo")
    val plainPassword = PlainPassword.generate("hogeHoge123")

    val accountRepository: AccountRepository[Id] = new AccountRepositoryByMemoryWithId()
    val encryptService: EncryptService[Id] = new EncryptService[Id] {
      override def encrypt(value: String): Id[String] = value match {
        case plainPassword.value.value => "xxx"
        case _                         => "yyy"
      }
      override def matches(value0: String, value1: String): Id[Boolean] = encrypt(value0) == value1
    }
    val tokenService: TokenService[Id] = new TokenService[Id] {
      override def generate: Id[String] = "token"
    }

    val useCase = new SignInUseCase[Id](
      accountRepository,
      encryptService,
      tokenService
    )
    import usecases.SampleInterfacesLayer.SampleErrors._

    "execute" in {
      assert(accountRepository.store(Account.generate(accuntId, email, accountName, EncryptedPassword("xxx"))) === 1L)

      val result = useCase.execute(SignInInput(email, plainPassword))
      assert(result.token === "token")

      assertThrows[Exception](useCase.execute(SignInInput(Email.generate("hoge@hoge.com"), plainPassword)))
      assertThrows[Exception](useCase.execute(SignInInput(email, PlainPassword.generate("fugaFuga123"))))
    }

  }

}
