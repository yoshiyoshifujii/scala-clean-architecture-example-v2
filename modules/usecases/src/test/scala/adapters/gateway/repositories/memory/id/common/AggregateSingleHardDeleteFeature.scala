package adapters.gateway.repositories.memory.id.common

import cats.Id
import com.github.j5ik2o.dddbase.{ AggregateSingleHardDeletable, AggregateSingleWriter }

trait AggregateSingleHardDeleteFeature extends AggregateSingleHardDeletable[Id] with AggregateBaseWriteFeature {
  this: AggregateSingleWriter[Id] =>

  override def hardDelete(id: IdType): Id[Long] = dao.delete(id.value.toString)
}
