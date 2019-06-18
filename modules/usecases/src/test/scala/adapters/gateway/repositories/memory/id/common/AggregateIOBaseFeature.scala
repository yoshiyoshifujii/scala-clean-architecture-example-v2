package adapters.gateway.repositories.memory.id.common

import cats.Id
import com.github.j5ik2o.dddbase.AggregateIO
import com.github.j5ik2o.dddbase.memory.MemoryDaoSupport

trait AggregateIOBaseFeature extends AggregateIO[Id] {
  type RecordType <: MemoryDaoSupport#Record
  type DaoType <: MemoryDaoSupport#Dao[Id, RecordType]

  protected val dao: DaoType
}
