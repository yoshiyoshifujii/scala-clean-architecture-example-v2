package usecases.signed

import cats.implicits._
import domain.account.Auth
import domain.common.Email
import domain.customer._
import repositories.CustomerRepository
import usecases.{ UseCase, UseCaseApplicationError, UseCaseMonadError }

case class CustomerCreateInput(auth: Auth, code: CustomerCode, name: CustomerName, email: Email)
case class CustomerCreateOutput(id: CustomerId)

class CustomerCreateUseCase[F[_]](customerRepository: CustomerRepository[F])
    extends UseCase[F, CustomerCreateInput, CustomerCreateOutput] {

  override def execute(inputData: CustomerCreateInput)(implicit ME: UseCaseMonadError[F]): F[CustomerCreateOutput] =
    for {
      maybe <- customerRepository.findBy(inputData.code)
      stored <- maybe match {
        case None =>
          val generated = Customer.generate(
            CustomerId(),
            inputData.code,
            inputData.name,
            inputData.email,
            inputData.auth.accountId
          )
          customerRepository.store(generated).map(_ => generated)
        case Some(_) =>
          ME.raiseError[GeneratedCustomer](UseCaseApplicationError("already exists"))
      }
    } yield CustomerCreateOutput(stored.id)
}
