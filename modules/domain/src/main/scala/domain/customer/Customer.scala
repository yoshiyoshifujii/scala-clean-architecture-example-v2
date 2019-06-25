package domain.customer

import com.github.j5ik2o.dddbase.Aggregate
import domain.account.Account
import domain.common.{ DateTime, Email }

import scala.reflect._

sealed trait Customer extends Aggregate {
  override type AggregateType = Customer
  override type IdType        = CustomerId
  override protected val tag: ClassTag[Customer] = classTag[Customer]

  val name: CustomerName
  val email: Email
  val creator: Account
  val created: DateTime
  val updater: Account
  val updated: DateTime
  val version: BigInt
}

case class GeneratedCustomer private[customer] (
    id: CustomerId,
    name: CustomerName,
    email: Email,
    creator: Account,
    created: DateTime,
    updater: Account,
    updated: DateTime,
    version: BigInt = 1
) extends Customer

case class ResolvedCustomer private[customer] (
    id: CustomerId,
    name: CustomerName,
    email: Email,
    creator: Account,
    created: DateTime,
    updater: Account,
    updated: DateTime,
    version: BigInt
) extends Customer

object Customer {

  val generate: (CustomerId, CustomerName, Email, Account) => GeneratedCustomer = (id, name, email, creator) =>
    GeneratedCustomer(id, name, email, creator, DateTime.now, creator, DateTime.now)

  val generateResolved
    : (CustomerId, CustomerName, Email, Account, DateTime, Account, DateTime, BigInt) => ResolvedCustomer =
    ResolvedCustomer.apply

  val rename: (ResolvedCustomer, CustomerName) => ResolvedCustomer = (customer, name) => customer.copy(name = name)

  val changeEmail: (ResolvedCustomer, Email) => ResolvedCustomer = (customer, email) => customer.copy(email = email)

  val versionUp: (ResolvedCustomer, Account) => ResolvedCustomer = (customer, updater) =>
    customer.copy(updater = updater, updated = DateTime.now, version = customer.version + 1)
}
