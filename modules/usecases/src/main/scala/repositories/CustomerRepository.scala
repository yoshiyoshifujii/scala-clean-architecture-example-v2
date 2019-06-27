package repositories

import com.github.j5ik2o.dddbase._
import domain.customer.{ Customer, CustomerCode, CustomerId, ResolvedCustomer }

trait CustomerRepository[F[_]]
    extends AggregateSingleReader[F]
    with AggregateSingleWriter[F]
    with AggregateAllReader[F]
    with AggregateSingleHardDeletable[F] {
  override type AggregateType = Customer
  override type IdType        = CustomerId

  def findBy(code: CustomerCode): F[Option[ResolvedCustomer]]
}
