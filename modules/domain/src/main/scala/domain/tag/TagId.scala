package domain.tag

import cats.implicits._
import com.github.j5ik2o.dddbase.AggregateStringId
import domain.{ DomainError, DomainValidationResult, ValueObject }
import infrastructure.ulid.ULID

case class TagId(breachEncapsulationOfValue: ULID = ULID()) extends AggregateStringId {
  override val value: String = breachEncapsulationOfValue.asString
}

object TagId extends ValueObject[String, TagId] {
  override val validate: String => DomainValidationResult[TagId] =
    ULID
      .parseFromString(_)
      .fold(
        cause => DomainError(cause.toString).invalidNel,
        TagId(_).validNel
      )
}
