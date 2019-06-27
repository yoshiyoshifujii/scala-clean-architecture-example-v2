package domain.customer

import cats.implicits._
import domain.{ DomainError, DomainValidationResult, ValueObject }
import eu.timepit.refined.W
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.Size
import eu.timepit.refined.numeric.Interval

final case class CustomerCode private[customer] (value: CustomerCode.AsString)

object CustomerCode extends ValueObject[String, CustomerCode] {
  type AsString = String Refined Size[Interval.Closed[W.`1`.T, W.`10`.T]]

  override val validate: String => DomainValidationResult[CustomerCode] =
    applyRef[AsString](_).leftMap(DomainError).map(new CustomerCode(_)).toValidatedNel
}
