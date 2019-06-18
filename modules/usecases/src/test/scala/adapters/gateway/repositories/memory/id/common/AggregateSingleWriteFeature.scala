package adapters.gateway.repositories.memory.id.common

import cats.Id
import com.github.j5ik2o.dddbase.AggregateSingleWriter

trait AggregateSingleWriteFeature extends AggregateSingleWriter[Id] with AggregateBaseWriteFeature {

  override def store(aggregate: AggregateType): Id[Long] = {
    dao.set(convertToRecord(aggregate))
  }

}
