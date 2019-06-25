package domain.building

import com.github.j5ik2o.dddbase.Aggregate
import domain.account.Account
import domain.common.DateTime

import scala.reflect._

sealed trait Building extends Aggregate {
  override type AggregateType = Building
  override type IdType        = BuildingId
  override protected val tag: ClassTag[Building] = classTag[Building]

  val name: BuildingName
  val address: Address
  val creator: Account
  val created: DateTime
  val updater: Account
  val updated: DateTime
  val version: BigInt
}

case class GeneratedBuilding private[building] (
    id: BuildingId,
    name: BuildingName,
    address: Address,
    creator: Account,
    created: DateTime,
    updater: Account,
    updated: DateTime,
    version: BigInt = 1
) extends Building

case class ResolvedBuilding private[building] (
    id: BuildingId,
    name: BuildingName,
    address: Address,
    creator: Account,
    created: DateTime,
    updater: Account,
    updated: DateTime,
    version: BigInt
) extends Building

object Building {

  val generate: (BuildingId, BuildingName, Address, Account) => GeneratedBuilding = (id, name, address, creator) =>
    GeneratedBuilding(id, name, address, creator, DateTime.now, creator, DateTime.now)

  val generateResolved
    : (BuildingId, BuildingName, Address, Account, DateTime, Account, DateTime, BigInt) => ResolvedBuilding =
    ResolvedBuilding.apply

  val rename: (ResolvedBuilding, BuildingName) => ResolvedBuilding = (building, name) => building.copy(name = name)

  val changeAddress: (ResolvedBuilding, Address) => ResolvedBuilding = (building, address) =>
    building.copy(address = address)

  val versionUp: (ResolvedBuilding, Account) => ResolvedBuilding = (building, updater) =>
    building.copy(updater = updater, updated = DateTime.now, version = building.version + 1)
}
