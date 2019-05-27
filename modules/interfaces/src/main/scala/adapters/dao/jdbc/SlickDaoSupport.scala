package adapters.dao.jdbc

import java.time.Instant

import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

trait SlickDaoSupport {

  val profile: slick.jdbc.JdbcProfile

  import profile.api._

  implicit def instantColumnType: JdbcType[Instant] with BaseTypedType[Instant] =
    MappedColumnType.base[Instant, java.sql.Timestamp](
      { instant =>
        new java.sql.Timestamp(instant.toEpochMilli)
      }, { ts =>
        Instant.ofEpochMilli(ts.getTime)
      }
    )

  trait Record

  trait SoftDeletableRecord extends Record {
    val deleted: Boolean
  }

  abstract class TableBase[T](_tableTag: Tag, _tableName: String, _schemaName: Option[String] = None)
      extends Table[T](_tableTag, _schemaName, _tableName)

  trait SoftDeletableTableSupport[T] { this: TableBase[T] =>
    def deleted: Rep[Boolean]
  }
}
