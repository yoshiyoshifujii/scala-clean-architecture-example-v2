package adapters.dao.jdbc

import slick.jdbc.JdbcProfile

trait RDB {
  val rdbService: RDB.Service
}

object RDB {
  trait Service {
    val profile: JdbcProfile
    val db: JdbcProfile#Backend#Database
  }

  class Live(_profile: JdbcProfile, _db: JdbcProfile#Backend#Database) extends RDB {
    override val rdbService: RDB.Service = new Service {
      override val profile = _profile
      override val db      = _db
    }
  }
}
