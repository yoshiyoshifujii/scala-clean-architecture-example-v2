package adapters.dao.memory

import cats.Monad
import com.github.j5ik2o.dddbase.memory.MemoryDaoSupport
import com.google.common.base.Ticker
import com.google.common.cache._

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

trait GuavaMemoryDaoSupport extends MemoryDaoSupport {

  val DELETED = "deleted"

  object GuavaCacheBuilder {

    def build[K, V <: SoftDeletableRecord](
        concurrencyLevel: Option[Int] = None,
        expireAfterAccess: Option[Duration] = None,
        expireAfterWrite: Option[Duration] = None,
        initialCapacity: Option[Int] = None,
        maximumSize: Option[Int] = None,
        maximumWeight: Option[Int] = None,
        recordStats: Option[Boolean] = None,
        refreshAfterWrite: Option[Duration] = None,
        removalListener: Option[RemovalNotification[String, V] => Unit] = None,
        softValues: Option[Boolean] = None,
        ticker: Option[Ticker] = None,
        weakKeys: Option[Boolean] = None,
        weakValues: Option[Boolean] = None,
        weigher: Option[(String, V) => Int] = None
    ): Cache[K, V] = {
      {
        val b  = CacheBuilder.newBuilder()
        val b0 = maximumSize.fold(b)(v => b.initialCapacity(v))
        val b1 = concurrencyLevel.fold(b0)(v => b0.concurrencyLevel(v))
        val b2 = maximumSize.fold(b1)(v => b1.maximumSize(v.toLong))
        val b3 = expireAfterWrite.fold(b2)(v => b2.expireAfterWrite(v.length, v.unit))
        val b4 = maximumWeight.fold(b3)(v => b3.maximumWeight(v.toLong))
        val b5 = expireAfterAccess.fold(b4)(v => b4.expireAfterAccess(v.length, v.unit))
        val b6 = refreshAfterWrite.fold(b5)(v => b5.refreshAfterWrite(v.length, v.unit))
        val b7 = removalListener.fold(b6)(
          v =>
            b6.removalListener(new RemovalListener[AnyRef, AnyRef] {
              override def onRemoval(
                  removalNotification: RemovalNotification[
                    AnyRef,
                    AnyRef
                  ]
              ): Unit = v(removalNotification.asInstanceOf[RemovalNotification[String, V]])
            })
        )
        val b8  = softValues.fold(b7)(v => if (v) b7.softValues() else b7)
        val b9  = weakValues.fold(b8)(v => if (v) b8.weakValues() else b8)
        val b10 = weakKeys.fold(b9)(v => if (v) b9.weakKeys() else b9)
        val b11 = weigher.fold(b10)(
          f =>
            b10.weigher(new Weigher[AnyRef, AnyRef] {
              override def weigh(k: AnyRef, v: AnyRef): Int = f(k.asInstanceOf[String], v.asInstanceOf[V])
            })
        )
        val b12 = recordStats.fold(b11)(v => if (v) b11.recordStats() else b11)
        val b13 = ticker.fold(b12)(v => b12.ticker(v))
        b13
      }.build().asInstanceOf[Cache[K, V]]
    }
  }

  abstract class GuavaCacheDao[K, V <: SoftDeletableRecord, F[_]](cache: Cache[String, V])(implicit M: Monad[F])
      extends Dao[F, V]
      with DaoSoftDeletable[F, V] {
    import cats.implicits._

    override def set(record: V): F[Long] = {
      cache.put(record.id, record)
      M.pure(1L)
    }

    override def setMulti(records: Seq[V]): F[Long] = {
      cache.putAll(records.map(v => (v.id, v)).toMap.asJava)
      M.pure(records.size.toLong)
    }

    override def get(id: String): F[Option[V]] = {
      M.pure(Option(cache.getIfPresent(id)).filterNot(_.status == DELETED))
    }

    override def getAll: F[Seq[V]] = {
      M.pure(cache.asMap().asScala.values.filterNot(_.status == DELETED).toSeq)
    }

    override def getMulti(ids: Seq[String]): F[Seq[V]] = {
      M.pure(cache.getAllPresent(ids.asJava).asScala.values.filterNot(_.status == DELETED).toSeq)
    }

    override def delete(id: String): F[Long] = {
      cache.invalidate(id)
      M.pure(1L)
    }

    override def deleteMulti(ids: Seq[String]): F[Long] = {
      cache.invalidateAll(ids.asJava)
      M.pure(ids.size.toLong)
    }

    override def softDelete(id: String): F[Long] = get(id).flatMap {
      case Some(v) => set(v.withStatus(DELETED).asInstanceOf[V])
      case None    => M.pure(0L)
    }

    override def softDeleteMulti(ids: Seq[String]): F[Long] = {
      getMulti(ids).flatMap { values =>
        setMulti(values.map(_.withStatus(DELETED).asInstanceOf[V]))
      }
    }

  }

}
