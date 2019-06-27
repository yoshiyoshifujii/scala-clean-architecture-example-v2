package domain.customer

import com.github.j5ik2o.dddbase.Aggregate
import domain.account.AccountId
import domain.common.{ DateTime, Email }

import scala.reflect._

sealed trait Customer extends Aggregate {
  override type AggregateType = Customer
  override type IdType        = CustomerId
  override protected val tag: ClassTag[Customer] = classTag[Customer]

  val code: CustomerCode
  val name: CustomerName
  val email: Email
  val creator: AccountId
  val created: DateTime
  val updater: AccountId
  val updated: DateTime
  val version: BigInt
}

case class GeneratedCustomer private[customer] (
    id: CustomerId,
    code: CustomerCode,
    name: CustomerName,
    email: Email,
    creator: AccountId,
    created: DateTime,
    updater: AccountId,
    updated: DateTime,
    version: BigInt = 1
) extends Customer

case class ResolvedCustomer private[customer] (
    id: CustomerId,
    code: CustomerCode,
    name: CustomerName,
    email: Email,
    creator: AccountId,
    created: DateTime,
    updater: AccountId,
    updated: DateTime,
    version: BigInt
) extends Customer

object Customer {

  val generate: (CustomerId, CustomerCode, CustomerName, Email, AccountId) => GeneratedCustomer =
    (id, code, name, email, creator) =>
      GeneratedCustomer(id, code, name, email, creator, DateTime.now, creator, DateTime.now)

  val generateResolved: (
      CustomerId,
      CustomerCode,
      CustomerName,
      Email,
      AccountId,
      DateTime,
      AccountId,
      DateTime,
      BigInt
  ) => ResolvedCustomer =
    ResolvedCustomer.apply

  val rename: (ResolvedCustomer, CustomerName) => ResolvedCustomer = (customer, name) => customer.copy(name = name)

  val changeEmail: (ResolvedCustomer, Email) => ResolvedCustomer = (customer, email) => customer.copy(email = email)

  val versionUp: (ResolvedCustomer, AccountId) => ResolvedCustomer = (customer, updater) =>
    customer.copy(updater = updater, updated = DateTime.now, version = customer.version + 1)
}
