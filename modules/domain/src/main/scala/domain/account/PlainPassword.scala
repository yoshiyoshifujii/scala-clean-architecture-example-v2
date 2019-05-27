package domain.account

import cats.implicits._
import domain.{ DomainError, DomainValidationResult, ValueObject }
import eu.timepit.refined.W
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex

final case class PlainPassword(value: PlainPassword.AsString)

object PlainPassword extends ValueObject[String, PlainPassword] {
  type AsString = String Refined MatchesRegex[W.`"[0-9a-zA-Z]{8,48}"`.T]

  override val validate: String => DomainValidationResult[PlainPassword] =
    applyRef[AsString](_).leftMap(DomainError).map(new PlainPassword(_)).toValidatedNel

}
