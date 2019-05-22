package usecases.account

import cats.implicits._
import domain.account.{ Account, AccountName, EncryptedPassword, PlainPassword }
import domain.common.Email
import gateway.generators.AccountIdGenerator
import gateway.repositories.AccountRepository
import gateway.services.EncryptService
import usecases.{ UseCase, UseCaseMonadError, _ }

case class AccountCreateInput(email: String, password: String, name: String)
case class AccountCreateOutput(id: String)

class AccountCreateUseCase[F[_]](
    accountIdGenerator: AccountIdGenerator[F],
    accountRepository: AccountRepository[F],
    encryptService: EncryptService[F]
) extends UseCase[F, AccountCreateInput, AccountCreateOutput] {

  override def execute(inputData: AccountCreateInput)(implicit ME: UseCaseMonadError[F]): F[AccountCreateOutput] =
    for {
      email    <- Email.generate(inputData.email).toF
      password <- PlainPassword.generate(inputData.password).toF
      name     <- AccountName.generate(inputData.name).toF
      maybe    <- accountRepository.findBy(email)
      stored <- maybe match {
        case Some(account) => ME.pure(account)
        case None =>
          for {
            id        <- accountIdGenerator.generate
            encrypted <- encryptService.encrypt(password.value.value)
            _ <- accountRepository.store(
              Account.generate(id, email, name, EncryptedPassword(encrypted))
            )
          } yield Account.generateResolved(id, email, name, EncryptedPassword(encrypted))
      }
    } yield AccountCreateOutput(stored.id.value)

}
