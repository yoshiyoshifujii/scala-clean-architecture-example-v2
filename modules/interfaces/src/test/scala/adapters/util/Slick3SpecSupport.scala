package adapters.util

import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfter, BeforeAndAfterAll, Suite }
import slick.basic.DatabaseConfig
import slick.jdbc.SetParameter.SetUnit
import slick.jdbc.{ JdbcProfile, SQLActionBuilder }

import scala.concurrent.ExecutionContext

trait Slick3SpecSupport extends BeforeAndAfter with BeforeAndAfterAll with ScalaFutures with JdbcSpecSupport {
  self: Suite with FlywayWithMySQLSpecSupport =>

  private var _dbConfig: DatabaseConfig[JdbcProfile] = _

  protected def dbConfig: DatabaseConfig[JdbcProfile] = _dbConfig

  after {
    val profile = dbConfig.profile
    import profile.api._
    implicit val ec: ExecutionContext = dbConfig.db.executor.executionContext
    val actions = tables.map { table =>
      SQLActionBuilder(List(s"TRUNCATE TABLE $table"), SetUnit).asUpdate
    }
    dbConfig.db.run(DBIO.sequence(actions).transactionally)
  }

  override def beforeAll: Unit = {
    super.beforeAll()
    val config = ConfigFactory.parseString(s"""
                                              |slick {
                                              |  profile = "slick.jdbc.MySQLProfile$$"
                                              |  db {
                                              |    connectionPool = disabled
                                              |    driver = "com.mysql.jdbc.Driver"
                                              |    url = "jdbc:mysql://localhost:$jdbcPort/$dbName?useSSL=false"
                                              |    user = "$dbUser"
                                              |    password = "$dbPassword"
                                              |  }
                                              |}
      """.stripMargin)
    _dbConfig = DatabaseConfig.forConfig[JdbcProfile]("slick", config)
  }

  override def afterAll: Unit = {
    dbConfig.db.shutdown
    super.afterAll()
  }

}
