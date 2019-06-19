package adapters.gateway.repositories.memory.zio.common

import adapters.Effect
import com.github.j5ik2o.dddbase.{ AggregateSingleHardDeletable, AggregateSingleWriter }

trait AggregateSingleHardDeleteFeature extends AggregateSingleHardDeletable[Effect] with AggregateBaseWriteFeature {
  this: AggregateSingleWriter[Effect] =>
  override def hardDelete(id: IdType): Effect[Long] = dao.delete(id.value.toString)
}
