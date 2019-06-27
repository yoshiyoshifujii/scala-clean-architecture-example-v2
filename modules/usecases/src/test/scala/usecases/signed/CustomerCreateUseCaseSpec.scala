package usecases.signed

import adapters.gateway.repositories.memory.id.CustomerRepositoryByMemoryWithId
import cats.Id
import domain.account.{ AccountId, Auth }
import domain.common.Email
import domain.customer.{ CustomerCode, CustomerName }
import org.scalatest.{ DiagrammedAssertions, FreeSpec }

class CustomerCreateUseCaseSpec extends FreeSpec with DiagrammedAssertions {

  "CustomerCreateUseCase" - {

    val repository = new CustomerRepositoryByMemoryWithId()
    val useCase    = new CustomerCreateUseCase[Id](repository)

    val auth  = Auth(AccountId())
    val code  = CustomerCode.generate("C-001")
    val name  = CustomerName.generate("hoge hogeo")
    val email = Email.generate("a@a.com")

    import usecases.SampleInterfacesLayer.SampleErrors._
    "execute" in {
      val output = useCase.execute(CustomerCreateInput(auth, code, name, email))
      assert(output.id.breachEncapsulationOfValue.asString.nonEmpty)

      assertThrows[Exception](useCase.execute(CustomerCreateInput(auth, code, name, email)))
    }

  }

}
