package adapters.http.directives

import adapters.http.json.{ AccountUpdateRequestJsonWithId, SignInRequestJson, SignUpRequestJson }
import adapters.http.rejections.ValidationRejections
import adapters.validator.{ ValidationResult, Validator }
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import domain.account.{ AccountId, AccountName, PlainPassword }
import domain.common.Email
import usecases.anonymous.{ SignInInput, SignUpInput }
import usecases.signed.AccountUpdateInput

trait ValidateDirectives {

  def validateJsonRequest[A, B](value: A)(implicit V: Validator[A, B]): Directive1[B] =
    V.validate(value)
      .fold({ errors =>
        reject(ValidationRejections(errors))
      }, provide)

}

object ValidateDirectives extends ValidateDirectives {
  import cats.implicits._

  implicit object SignUpRequestJsonValidator extends Validator[SignUpRequestJson, SignUpInput] {
    override def validate(value: SignUpRequestJson): ValidationResult[SignUpInput] =
      (
        Email.validate(value.email),
        PlainPassword.validate(value.password),
        AccountName.validate(value.name)
      ).mapN {
        case (email, password, name) =>
          SignUpInput(email, password, name)
      }
  }

  implicit object SignInRequestJsonValidator extends Validator[SignInRequestJson, SignInInput] {
    override def validate(value: SignInRequestJson): ValidationResult[SignInInput] =
      (
        Email.validate(value.email),
        PlainPassword.validate(value.password)
      ).mapN {
        case (email, password) =>
          SignInInput(email, password)
      }
  }

  implicit object AccountUpdateRequestJsonWithIdValidator
      extends Validator[AccountUpdateRequestJsonWithId, AccountUpdateInput] {
    override def validate(value: AccountUpdateRequestJsonWithId): ValidationResult[AccountUpdateInput] =
      (
        AccountName.validate(value.request.name),
        AccountId.validate(value.accountId)
      ).mapN {
        case (name, accountId) =>
          AccountUpdateInput(value.auth, accountId, name)
      }
  }

}
