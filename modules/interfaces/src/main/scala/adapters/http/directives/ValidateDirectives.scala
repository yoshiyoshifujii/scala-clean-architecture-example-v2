package adapters.http.directives

import adapters.http.json.CreateAccountRequestJson
import adapters.http.rejections.ValidationRejections
import adapters.validator.{ ValidationResult, Validator }
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import domain.account.{ AccountName, PlainPassword }
import domain.common.Email
import usecases.anonymous.SignUpInput

trait ValidateDirectives {

  def validateJsonRequest[A, B](value: A)(implicit V: Validator[A, B]): Directive1[B] =
    V.validate(value)
      .fold({ errors =>
        reject(ValidationRejections(errors))
      }, provide)

}

object ValidateDirectives extends ValidateDirectives {
  import cats.implicits._

  implicit object CreateAccountRequestJsonValidator extends Validator[CreateAccountRequestJson, SignUpInput] {
    override def validate(value: CreateAccountRequestJson): ValidationResult[SignUpInput] =
      (
        Email.validate(value.email),
        PlainPassword.validate(value.password),
        AccountName.validate(value.name)
      ).mapN {
        case (email, password, name) =>
          SignUpInput(email, password, name)
      }
  }

}
