package adapters.dao.memory

import java.time.ZonedDateTime

import cats.Monad
import com.google.common.base.Ticker
import com.google.common.cache.{ Cache, RemovalNotification }

import scala.concurrent.duration.Duration

trait CustomerComponent extends GuavaMemoryDaoSupport {

  case class CustomerRecord(
      id: String,
      code: String,
      name: String,
      email: String,
      creator: String,
      created: ZonedDateTime,
      updater: String,
      updated: ZonedDateTime,
      version: BigInt
  ) extends SoftDeletableRecord {
    override type This = CustomerRecord
    override val status: String = ""

    override def withStatus(value: String): CustomerRecord = this
  }

  case class CustomerDao[F[_]](cache: Cache[String, CustomerRecord])(implicit M: Monad[F])
      extends GuavaCacheDao[String, CustomerRecord, F](cache) {
    def this(
        concurrencyLevel: Option[Int] = None,
        expireAfterAccess: Option[Duration] = None,
        expireAfterWrite: Option[Duration] = None,
        initialCapacity: Option[Int] = None,
        maximumSize: Option[Int] = None,
        maximumWeight: Option[Int] = None,
        recordStats: Option[Boolean] = None,
        refreshAfterWrite: Option[Duration] = None,
        removalListener: Option[RemovalNotification[String, CustomerRecord] => Unit] = None,
        softValues: Option[Boolean] = None,
        ticker: Option[Ticker] = None,
        weakKeys: Option[Boolean] = None,
        weakValues: Option[Boolean] = None,
        weigher: Option[(String, CustomerRecord) => Int] = None
    )(implicit M: Monad[F]) = {
      this(
        GuavaCacheBuilder
          .build[String, CustomerRecord](
            concurrencyLevel,
            expireAfterAccess,
            expireAfterWrite,
            initialCapacity,
            maximumSize,
            maximumWeight,
            recordStats,
            refreshAfterWrite,
            removalListener,
            softValues,
            ticker,
            weakKeys,
            weakValues,
            weigher
          )
      )(M)
    }
  }

}
