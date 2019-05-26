package repositories

import com.github.j5ik2o.dddbase.{ AggregateSingleReader, AggregateSingleWriter }
import domain.account.{ Account, AccountId, ResolvedAccount }
import domain.common.Email

trait AccountRepository[F[_]] extends AggregateSingleReader[F] with AggregateSingleWriter[F] {
  override type AggregateType = Account
  override type IdType        = AccountId

  def findBy(email: Email): F[Option[ResolvedAccount]]
}
