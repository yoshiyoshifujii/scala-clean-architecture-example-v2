package domain.account

import com.github.j5ik2o.dddbase.AggregateStringId
import infrastructure.ulid.ULID

case class AccountId(breachEncapsulationOfValue: ULID = ULID()) extends AggregateStringId {
  override val value: String = breachEncapsulationOfValue.asString
}
