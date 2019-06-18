package adapters.gateway.repositories.memory.zio.common

import adapters.Effect
import com.github.j5ik2o.dddbase.AggregateIO
import com.github.j5ik2o.dddbase.memory.MemoryDaoSupport

trait AggregateIOBaseFeature extends AggregateIO[Effect] {
  type RecordType <: MemoryDaoSupport#Record
  type DaoType <: MemoryDaoSupport#Dao[Effect, RecordType]

  protected val dao: DaoType
}
