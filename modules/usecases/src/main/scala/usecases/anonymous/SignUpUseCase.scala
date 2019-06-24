package usecases.anonymous

import cats.implicits._
import domain.account._
import domain.common.Email
import repositories.AccountRepository
import services.EncryptService
import usecases.{ UseCase, UseCaseApplicationError, UseCaseMonadError }

case class SignUpInput(email: Email, password: PlainPassword, name: AccountName)
case class SignUpOutput(id: AccountId)

class SignUpUseCase[F[_]](
    accountRepository: AccountRepository[F],
    encryptService: EncryptService[F]
) extends UseCase[F, SignUpInput, SignUpOutput] {

  override def execute(inputData: SignUpInput)(implicit ME: UseCaseMonadError[F]): F[SignUpOutput] =
    for {
      maybe <- accountRepository.findBy(inputData.email)
      generated <- maybe match {
        case None =>
          for {
            encrypted <- encryptService.encrypt(inputData.password.value.value)
            generated = Account.generate(AccountId(), inputData.email, inputData.name, EncryptedPassword(encrypted))
            _ <- accountRepository.store(generated)
          } yield generated
        case Some(_) =>
          ME.raiseError[GeneratedAccount](UseCaseApplicationError("already exists"))
      }
    } yield SignUpOutput(generated.id)

}
