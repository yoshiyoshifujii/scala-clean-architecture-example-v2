package adapters.http.directives

import adapters.http.json._
import adapters.http.rejections.ValidationRejection
import adapters.validator.{ ValidationResult, Validator }
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import domain.account.{ AccountId, AccountName, PlainPassword }
import domain.common.Email
import usecases.anonymous.{ SignInInput, SignUpInput }
import usecases.signed.{ AccountDeleteInput, AccountGetInput, AccountUpdateInput }

trait ValidateDirectives {

  def validateJsonRequest[A, B](value: A)(implicit V: Validator[A, B]): Directive1[B] =
    V.validate(value)
      .fold({ errors =>
        reject(ValidationRejection(errors))
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

  implicit object AccountGetRequestWithAuthValidator extends Validator[AccountGetRequestWithAuth, AccountGetInput] {
    override def validate(value: AccountGetRequestWithAuth): ValidationResult[AccountGetInput] =
      AccountId.validate(value.accountId).map { accountId =>
        AccountGetInput(value.auth, accountId)
      }
  }

  implicit object AccountUpdateRequestJsonWithAuthValidator
      extends Validator[AccountUpdateRequestJsonWithAuth, AccountUpdateInput] {
    override def validate(value: AccountUpdateRequestJsonWithAuth): ValidationResult[AccountUpdateInput] =
      (
        AccountName.validate(value.request.name),
        AccountId.validate(value.accountId)
      ).mapN {
        case (name, accountId) =>
          AccountUpdateInput(value.auth, accountId, name)
      }
  }

  implicit object AccountDeleteRequestWithAuthValidator
      extends Validator[AccountDeleteRequestWithAuth, AccountDeleteInput] {
    override def validate(value: AccountDeleteRequestWithAuth): ValidationResult[AccountDeleteInput] =
      AccountId.validate(value.accountId).map { accountId =>
        AccountDeleteInput(value.auth, accountId)
      }
  }

}
