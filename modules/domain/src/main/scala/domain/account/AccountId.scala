package domain.account

import cats.implicits._
import com.github.j5ik2o.dddbase.AggregateStringId
import domain.{ DomainError, DomainValidationResult, ValueObject }
import infrastructure.ulid.ULID

case class AccountId(breachEncapsulationOfValue: ULID = ULID()) extends AggregateStringId {
  override val value: String = breachEncapsulationOfValue.asString
}

object AccountId extends ValueObject[String, AccountId] {
  override val validate: String => DomainValidationResult[AccountId] =
    ULID
      .parseFromString(_)
      .fold(
        cause => DomainError(cause.toString).invalidNel,
        AccountId(_).validNel
      )
}
