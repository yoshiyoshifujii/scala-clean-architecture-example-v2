package adapters.gateway.repositories.memory.id.common

import cats.Id
import com.github.j5ik2o.dddbase.{ AggregateNotFoundException, AggregateSingleReader }

trait AggregateSingleReadFeature extends AggregateSingleReader[Id] with AggregateBaseReadFeature {

  override def resolveById(id: IdType): Id[AggregateType] = {
    dao.get(id.value.toString) match {
      case Some(v) => convertToAggregate(v)
      case None    => throw AggregateNotFoundException(id)
    }
  }

}
