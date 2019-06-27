package domain.tag

import cats.implicits._
import domain.{ DomainError, DomainValidationResult, ValueObject }
import eu.timepit.refined.W
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.Size
import eu.timepit.refined.numeric.Interval

final case class TagValue private[tag] (value: TagValue.AsString)

object TagValue extends ValueObject[String, TagValue] {
  type AsString = String Refined Size[Interval.Closed[W.`0`.T, W.`50`.T]]

  override val validate: String => DomainValidationResult[TagValue] =
    applyRef[AsString](_).leftMap(DomainError).map(new TagValue(_)).toValidatedNel
}
