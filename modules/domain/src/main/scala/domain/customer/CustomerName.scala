package domain.customer

import cats.implicits._
import domain.{ DomainError, DomainValidationResult, ValueObject }
import eu.timepit.refined.W
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.Size
import eu.timepit.refined.numeric.Interval

final case class CustomerName private[customer] (value: CustomerName.AsString)

object CustomerName extends ValueObject[String, CustomerName] {
  type AsString = String Refined Size[Interval.Closed[W.`1`.T, W.`50`.T]]

  override val validate: String => DomainValidationResult[CustomerName] =
    applyRef[AsString](_).leftMap(DomainError).map(new CustomerName(_)).toValidatedNel
}
