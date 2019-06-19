package adapters.gateway.repositories.slick

import adapters.Effect
import adapters.dao.jdbc.AccountComponent
import adapters.gateway.repositories.slick.common.{
  AggregateAllReadFeature,
  AggregateSingleHardDeleteFeature,
  AggregateSingleReadFeature,
  AggregateSingleWriteFeature
}
import domain.account.{ Account, AccountId, AccountName, EncryptedPassword }
import domain.common.Email
import infrastructure.ulid.ULID
import repositories.AccountRepository
import scalaz.zio.ZIO
import slick.jdbc.JdbcProfile
import slick.lifted.Rep

abstract class AbstractAccountRepositoryBySlick(val profile: JdbcProfile, val db: JdbcProfile#Backend#Database)
    extends AccountRepository[Effect]
    with AggregateSingleWriteFeature
    with AggregateSingleReadFeature
    with AggregateAllReadFeature
    with AggregateSingleHardDeleteFeature
    with AccountComponent {

  override type RecordType = AccountRecord
  override type TableType  = Accounts
  override protected val dao = AccountDao

  override protected def byCondition(id: AccountId): Accounts => Rep[Boolean] = {
    import profile.api._
    _.id === id.value
  }

  override protected def byConditions(ids: Seq[AccountId]): Accounts => Rep[Boolean] = {
    import profile.api._
    _.id.inSet(ids.map(_.value))
  }

  override protected def convertToRecord: Account => Effect[AccountRecord] = { aggregate =>
    ZIO.succeed {
      AccountRecord(
        aggregate.id.value,
        aggregate.email.value.value,
        aggregate.name.value.value,
        aggregate.password.value
      )
    }
  }

  override protected def convertToAggregate: AccountRecord => Effect[Account] = record => {
    ZIO.succeed {
      Account.generateResolved(
        AccountId(ULID.parseFromString(record.id).get),
        Email.generate(record.email),
        AccountName.generate(record.name),
        EncryptedPassword(record.password)
      )
    }
  }
}
