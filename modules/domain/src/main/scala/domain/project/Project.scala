package domain.project

import java.time.ZonedDateTime

import domain.account.Account
import domain.building.Building
import domain.customer.Customer

trait Project {

  val creator: Account
  val created: ZonedDateTime
  val updater: Account
  val updated: ZonedDateTime
  val customer: Customer
  val building: Building

}
