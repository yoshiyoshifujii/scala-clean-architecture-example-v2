package adapters.gateway.repositories.memory.id.common

import cats.Id
import com.github.j5ik2o.dddbase.AggregateAllReader

trait AggregateAllReadFeature extends AggregateAllReader[Id] with AggregateBaseReadFeature {
  override def resolveAll: Id[Seq[AggregateType]] = {
    dao.getAll.map(convertToAggregate)
  }
}
