package domain.constructionimage

import cats.implicits._
import com.github.j5ik2o.dddbase.AggregateStringId
import domain.{ DomainError, DomainValidationResult, ValueObject }
import infrastructure.ulid.ULID

case class ConstructionImageId(breachEncapsulationOfValue: ULID = ULID()) extends AggregateStringId {
  override val value: String = breachEncapsulationOfValue.asString
}

object ConstructionImageId extends ValueObject[String, ConstructionImageId] {
  override val validate: String => DomainValidationResult[ConstructionImageId] =
    ULID
      .parseFromString(_)
      .fold(
        cause => DomainError(cause.toString).invalidNel,
        ConstructionImageId(_).validNel
      )
}
