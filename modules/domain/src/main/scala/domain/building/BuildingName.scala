package domain.building

import cats.implicits._
import domain.{ DomainError, DomainValidationResult, ValueObject }
import eu.timepit.refined.W
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.Size
import eu.timepit.refined.numeric.Interval

case class BuildingName private[building] (value: BuildingName.AsString)

object BuildingName extends ValueObject[String, BuildingName] {
  type AsString = String Refined Size[Interval.Closed[W.`1`.T, W.`50`.T]]

  override val validate: String => DomainValidationResult[BuildingName] =
    applyRef[AsString](_).leftMap(DomainError).map(new BuildingName(_)).toValidatedNel
}
