package domain.building

import cats.implicits._
import domain.{ DomainError, DomainValidationResult, ValueObject }
import eu.timepit.refined.W
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex

case class ZipCode private[building] (value: ZipCode.AsString)

object ZipCode extends ValueObject[String, ZipCode] {
  type AsString = String Refined MatchesRegex[W.`"[0-9]{3}-[0-9]{4}"`.T]

  override val validate: String => DomainValidationResult[ZipCode] =
    applyRef[AsString](_).leftMap(DomainError).map(new ZipCode(_)).toValidatedNel
}
