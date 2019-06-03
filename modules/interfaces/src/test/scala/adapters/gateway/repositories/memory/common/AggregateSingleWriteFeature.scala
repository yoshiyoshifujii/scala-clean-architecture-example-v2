package adapters.gateway.repositories.memory.common

import adapters.Effect
import com.github.j5ik2o.dddbase.AggregateSingleWriter

trait AggregateSingleWriteFeature extends AggregateSingleWriter[Effect] with AggregateBaseWriteFeature {

  override def store(aggregate: AggregateType): Effect[Long] = {
    for {
      record <- convertToRecord(aggregate)
      result <- dao.set(record)
    } yield result
  }

}
