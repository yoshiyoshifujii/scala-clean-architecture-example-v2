package adapters.dao.memory

import com.google.common.base.Ticker
import com.google.common.cache.{ Cache, RemovalNotification }

import scala.concurrent.duration.Duration

trait AccountComponent extends GuavaMemoryDaoSupport {

  case class AccountRecord(
      id: String,
      email: String,
      password: String,
      name: String
  ) extends SoftDeletableRecord {
    override type This = AccountRecord
    override val status: String = ""

    override def withStatus(value: String): AccountRecord = this
  }

  case class AccountDao(cache: Cache[String, AccountRecord]) extends GuavaCacheDao[String, AccountRecord](cache) {
    def this(
        concurrencyLevel: Option[Int] = None,
        expireAfterAccess: Option[Duration] = None,
        expireAfterWrite: Option[Duration] = None,
        initialCapacity: Option[Int] = None,
        maximumSize: Option[Int] = None,
        maximumWeight: Option[Int] = None,
        recordStats: Option[Boolean] = None,
        refreshAfterWrite: Option[Duration] = None,
        removalListener: Option[RemovalNotification[String, AccountRecord] => Unit] = None,
        softValues: Option[Boolean] = None,
        ticker: Option[Ticker] = None,
        weakKeys: Option[Boolean] = None,
        weakValues: Option[Boolean] = None,
        weigher: Option[(String, AccountRecord) => Int] = None
    ) = {
      this(
        GuavaCacheBuilder
          .build[String, AccountRecord](
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
      )
    }
  }

}
