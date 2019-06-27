package domain.project

import cats.implicits._
import com.github.j5ik2o.dddbase.AggregateStringId
import domain.{ DomainError, DomainValidationResult, ValueObject }
import infrastructure.ulid.ULID

case class ProjectId(breachEncapsulationOfValue: ULID = ULID()) extends AggregateStringId {
  override val value: String = breachEncapsulationOfValue.asString
}

object ProjectId extends ValueObject[String, ProjectId] {
  override val validate: String => DomainValidationResult[ProjectId] =
    ULID
      .parseFromString(_)
      .fold(
        cause => DomainError(cause.toString).invalidNel,
        ProjectId(_).validNel
      )
}
