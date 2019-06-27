package domain.tag

import cats.implicits._
import domain.{ DomainError, DomainValidationResult, ValueObject }
import eu.timepit.refined.W
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.Size
import eu.timepit.refined.numeric.Interval

final case class TagKey private[customer] (value: TagKey.AsString)

object TagKey extends ValueObject[String, TagKey] {
  type AsString = String Refined Size[Interval.Closed[W.`1`.T, W.`50`.T]]

  override val validate: String => DomainValidationResult[TagKey] =
    applyRef[AsString](_).leftMap(DomainError).map(new TagKey(_)).toValidatedNel
}
