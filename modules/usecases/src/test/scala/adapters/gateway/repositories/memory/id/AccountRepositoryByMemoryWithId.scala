package adapters.gateway.repositories.memory.id

import adapters.dao.memory.AccountComponent
import adapters.gateway.repositories.memory.id.common.{
  AggregateAllReadFeature,
  AggregateSingleReadFeature,
  AggregateSingleWriteFeature
}
import cats.Id
import com.google.common.base.Ticker
import domain.account._
import domain.common.Email
import infrastructure.ulid.ULID
import repositories.AccountRepository

import scala.concurrent.duration.Duration

class AccountRepositoryByMemoryWithId(
    concurrencyLevel: Option[Int] = None,
    expireAfterAccess: Option[Duration] = None,
    expireAfterWrite: Option[Duration] = None,
    initialCapacity: Option[Int] = None,
    maximumSize: Option[Int] = None,
    maximumWeight: Option[Int] = None,
    recordStats: Option[Boolean] = None,
    refreshAfterWrite: Option[Duration] = None,
    softValues: Option[Boolean] = None,
    ticker: Option[Ticker] = None,
    weakKeys: Option[Boolean] = None,
    weakValues: Option[Boolean] = None
) extends AccountRepository[Id]
    with AggregateSingleReadFeature
    with AggregateSingleWriteFeature
    with AggregateAllReadFeature
    with AccountComponent {

  override type RecordType = AccountRecord
  override type DaoType    = AccountDao[Id]

  import cats.implicits._

  override protected val dao: AccountDao[Id] =
    new AccountDao(
      concurrencyLevel = concurrencyLevel,
      expireAfterAccess = expireAfterAccess,
      expireAfterWrite = expireAfterWrite,
      initialCapacity = initialCapacity,
      maximumSize = maximumSize,
      maximumWeight = maximumWeight,
      recordStats = recordStats,
      refreshAfterWrite = refreshAfterWrite,
      softValues = softValues,
      ticker = ticker,
      weakKeys = weakKeys,
      weakValues = weakValues
    )

  override protected def convertToRecord: AggregateType => Id[RecordType] =
    aggregate =>
      AccountRecord(
        aggregate.id.value,
        aggregate.email.value.value,
        aggregate.password.value,
        aggregate.name.value.value
      )

  override protected def convertToAggregate: RecordType => Id[AggregateType] =
    record =>
      Account.generateResolved(
        AccountId(ULID.parseFromString(record.id).get),
        Email.generate(record.email),
        AccountName.generate(record.name),
        EncryptedPassword(record.password)
      )

  override def findBy(email: Email): Id[Option[ResolvedAccount]] = {
    dao.getAll.find(_.email == email.value.value) match {
      case Some(record) => convertToAggregate(record).map(a => Some(a.asInstanceOf[ResolvedAccount]))
      case None         => None
    }
  }

}
