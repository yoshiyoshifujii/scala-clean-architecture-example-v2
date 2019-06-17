package repositories

import com.github.j5ik2o.dddbase.{ AggregateAllReader, AggregateSingleReader, AggregateSingleWriter }
import domain.account.{ Account, AccountId }
import domain.common.Email

trait AccountRepository[F[_]]
    extends AggregateSingleReader[F]
    with AggregateSingleWriter[F]
    with AggregateAllReader[F] {
  override type AggregateType = Account
  override type IdType        = AccountId

  def findBy(email: Email): F[Option[AggregateType]]
}
