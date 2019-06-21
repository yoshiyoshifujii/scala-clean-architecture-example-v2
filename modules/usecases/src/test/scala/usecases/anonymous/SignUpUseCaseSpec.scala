package usecases.anonymous

import adapters.gateway.repositories.memory.id.AccountRepositoryByMemoryWithId
import cats.Id
import domain.account._
import domain.common.Email
import org.scalatest.{ DiagrammedAssertions, FreeSpec }
import repositories.AccountRepository
import services.EncryptService

class SignUpUseCaseSpec extends FreeSpec with DiagrammedAssertions {

  "SignUpUseCase" - {
    val email1         = Email.generate("a@a.com")
    val accountName1   = AccountName.generate("hoge hogeo")
    val plainPassword1 = PlainPassword.generate("hogeHoge123")

    val accountRepository: AccountRepository[Id] = new AccountRepositoryByMemoryWithId()
    val encryptService: EncryptService[Id] = new EncryptService[Id] {
      override def encrypt(value: String): Id[String]                   = "xxx"
      override def matches(value0: String, value1: String): Id[Boolean] = ???
    }
    val useCase: SignUpUseCase[Id] = new SignUpUseCase[Id](
      accountRepository,
      encryptService
    )
    import usecases.SampleInterfacesLayer.SampleErrors._

    "success - new" in {
      val result = useCase.execute(SignUpInput(email = email1, password = plainPassword1, name = accountName1))
      assert {
        result.id.breachEncapsulationOfValue.asString.nonEmpty
      }
    }

  }

}
