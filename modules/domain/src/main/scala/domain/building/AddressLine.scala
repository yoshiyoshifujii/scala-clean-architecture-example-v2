package domain.building

import cats.implicits._
import domain.{ DomainError, DomainValidationResult, ValueObject }
import eu.timepit.refined.W
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.Size
import eu.timepit.refined.numeric.Interval

case class AddressLine private[building] (value: AddressLine.AsString)

object AddressLine extends ValueObject[String, AddressLine] {
  type AsString = String Refined Size[Interval.Closed[W.`1`.T, W.`50`.T]]

  override val validate: String => DomainValidationResult[AddressLine] =
    applyRef[AsString](_).leftMap(DomainError).map(new AddressLine(_)).toValidatedNel
}
