package adapters.gateway.repositories.slick.common

import adapters.Effect
import adapters.dao.jdbc.SlickDaoSupport
import com.github.j5ik2o.dddbase.AggregateIO
import slick.jdbc.JdbcProfile
import slick.lifted.{ Rep, TableQuery }

trait AggregateIOBaseFeature extends AggregateIO[Effect] {
  type RecordType <: SlickDaoSupport#Record
  type TableType <: SlickDaoSupport#TableBase[RecordType]

  protected val profile: JdbcProfile
  protected val db: JdbcProfile#Backend#Database
  protected val dao: TableQuery[TableType]

  protected def byCondition(id: IdType): TableType => Rep[Boolean]

  protected def byConditions(ids: Seq[IdType]): TableType => Rep[Boolean]

}
