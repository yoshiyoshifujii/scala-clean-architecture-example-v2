package domain.account

import cats.implicits._
import domain.{ DomainError, DomainValidationResult }
import eu.timepit.refined.W
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.Size
import eu.timepit.refined.numeric.Interval

final case class AccountName private[account] (value: AccountName.AsString)

object AccountName {
  type AsString = String Refined Size[Interval.Closed[W.`1`.T, W.`50`.T]]

  val generate: String => DomainValidationResult[AccountName] =
    applyRef[AsString](_).leftMap(DomainError).map(new AccountName(_)).toValidatedNel
}
