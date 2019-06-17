package adapters.gateway.repositories.memory.common

import adapters.Effect
import com.github.j5ik2o.dddbase.AggregateAllReader
import scalaz.zio.ZIO

trait AggregateAllReadFeature extends AggregateAllReader[Effect] with AggregateBaseReadFeature {
  override def resolveAll: Effect[Seq[AggregateType]] =
    for {
      records    <- dao.getAll
      aggregates <- ZIO.collectAll(records.map(convertToAggregate))
    } yield aggregates
}
