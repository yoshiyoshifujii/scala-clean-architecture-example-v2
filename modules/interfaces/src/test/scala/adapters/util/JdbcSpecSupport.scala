package adapters.util

import org.scalatest.concurrent.ScalaFutures

trait JdbcSpecSupport extends ScalaFutures {
  this: FlywayWithMySQLSpecSupport =>
  val tables: Seq[String]

  def jdbcPort: Int = mySQLdConfig.port.get

}
