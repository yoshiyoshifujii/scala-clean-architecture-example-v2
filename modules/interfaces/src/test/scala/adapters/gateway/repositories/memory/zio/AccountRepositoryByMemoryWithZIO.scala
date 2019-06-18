package adapters.gateway.repositories.memory.zio

import adapters.Effect
import adapters.dao.memory.AccountComponent
import adapters.gateway.repositories.memory.zio.common.{
  AggregateAllReadFeature,
  AggregateSingleReadFeature,
  AggregateSingleWriteFeature
}
import com.google.common.base.Ticker
import domain.account._
import domain.common.Email
import infrastructure.ulid.ULID
import repositories.AccountRepository
import scalaz.zio.ZIO

import scala.concurrent.duration.Duration

class AccountRepositoryByMemoryWithZIO(
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
) extends AccountRepository[Effect]
    with AggregateSingleReadFeature
    with AggregateSingleWriteFeature
    with AggregateAllReadFeature
    with AccountComponent {

  override type RecordType = AccountRecord
  override type DaoType    = AccountDao[Effect]

  import adapters.errors.Errors._

  override protected val dao: AccountDao[Effect] =
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

  override protected def convertToRecord: AggregateType => Effect[RecordType] =
    aggregate =>
      ZIO.succeed {
        AccountRecord(
          aggregate.id.value,
          aggregate.email.value.value,
          aggregate.password.value,
          aggregate.name.value.value
        )
      }

  override protected def convertToAggregate: RecordType => Effect[AggregateType] =
    record =>
      ZIO.succeed {
        Account.generateResolved(
          AccountId(ULID.parseFromString(record.id).get),
          Email.generate(record.email),
          AccountName.generate(record.name),
          EncryptedPassword(record.password)
        )
      }

  override def findBy(email: Email): Effect[Option[ResolvedAccount]] =
    for {
      records <- dao.getAll
      aggregate <- records.find(_.email == email.value.value) match {
        case Some(record) => convertToAggregate(record).map(a => Some(a.asInstanceOf[ResolvedAccount]))
        case None         => ZIO.succeed(None)
      }
    } yield aggregate

}
