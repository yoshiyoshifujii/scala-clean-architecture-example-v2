package adapters.gateway.repositories.memory.id

import adapters.dao.memory.CustomerComponent
import adapters.gateway.repositories.memory.id.common.{
  AggregateAllReadFeature,
  AggregateSingleHardDeleteFeature,
  AggregateSingleReadFeature,
  AggregateSingleWriteFeature
}
import cats.Id
import com.google.common.base.Ticker
import domain.account._
import domain.common.{ DateTime, Email }
import domain.customer._
import infrastructure.ulid.ULID
import repositories.CustomerRepository

import scala.concurrent.duration.Duration

class CustomerRepositoryByMemoryWithId(
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
) extends CustomerRepository[Id]
    with AggregateSingleReadFeature
    with AggregateSingleWriteFeature
    with AggregateAllReadFeature
    with AggregateSingleHardDeleteFeature
    with CustomerComponent {

  override type RecordType = CustomerRecord
  override type DaoType    = CustomerDao[Id]

  override protected val dao: CustomerDao[Id] =
    new CustomerDao(
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
      CustomerRecord(
        aggregate.id.value,
        aggregate.code.value.value,
        aggregate.name.value.value,
        aggregate.email.value.value,
        aggregate.creator.value,
        aggregate.created.value,
        aggregate.updater.value,
        aggregate.updated.value,
        aggregate.version
      )

  override protected def convertToAggregate: RecordType => Id[AggregateType] =
    record =>
      Customer.generateResolved(
        CustomerId(ULID.parseFromString(record.id).get),
        CustomerCode.generate(record.code),
        CustomerName.generate(record.name),
        Email.generate(record.email),
        AccountId(ULID.parseFromString(record.creator).get),
        DateTime(record.created),
        AccountId(ULID.parseFromString(record.updater).get),
        DateTime(record.updated),
        record.version
      )

  override def findBy(code: CustomerCode): Id[Option[ResolvedCustomer]] =
    dao.getAll.find(_.code == code.value.value).map(record => convertToAggregate(record).asInstanceOf[ResolvedCustomer])
}
