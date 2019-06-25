package domain.building

import cats.implicits._
import com.github.j5ik2o.dddbase.AggregateStringId
import domain.{ DomainError, DomainValidationResult, ValueObject }
import infrastructure.ulid.ULID

case class BuildingId(breachEncapsulationOfValue: ULID = ULID()) extends AggregateStringId {
  override val value: String = breachEncapsulationOfValue.asString
}

object BuildingId extends ValueObject[String, BuildingId] {
  override val validate: String => DomainValidationResult[BuildingId] =
    ULID
      .parseFromString(_)
      .fold(
        cause => DomainError(cause.toString).invalidNel,
        BuildingId(_).validNel
      )
}
