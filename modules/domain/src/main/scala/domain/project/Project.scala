package domain.project

import com.github.j5ik2o.dddbase.Aggregate
import domain.account.Account
import domain.building.Building
import domain.common.DateTime
import domain.customer.Customer

import scala.reflect._

sealed trait Project extends Aggregate {
  override type AggregateType = Project
  override type IdType        = ProjectId
  override protected val tag: ClassTag[Project] = classTag[Project]

  val customer: Customer
  val building: Building
  val creator: Account
  val created: DateTime
  val updater: Account
  val updated: DateTime
}

case class GeneratedProject private[project] (
    id: ProjectId,
    customer: Customer,
    building: Building,
    creator: Account,
    created: DateTime,
    updater: Account,
    updated: DateTime,
    version: BigInt = 1
) extends Project

case class ResolvedProject private[project] (
    id: ProjectId,
    customer: Customer,
    building: Building,
    creator: Account,
    created: DateTime,
    updater: Account,
    updated: DateTime,
    version: BigInt
) extends Project

object Project {

  val generate: (ProjectId, Customer, Building, Account) => GeneratedProject = (id, customer, building, creator) =>
    GeneratedProject(id, customer, building, creator, DateTime.now, creator, DateTime.now)

  val generateResolved
    : (ProjectId, Customer, Building, Account, DateTime, Account, DateTime, BigInt) => ResolvedProject =
    ResolvedProject.apply

}
