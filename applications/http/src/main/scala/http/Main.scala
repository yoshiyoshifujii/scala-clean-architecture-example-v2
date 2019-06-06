package http

import adapters.dao.jdbc.RDB
import adapters.http.controllers.Controller
import adapters.{ AppType, DISettings }
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContextExecutor

object Main extends App {

  private val host   = "localhost"
  private val port   = 8000
  private val config = ConfigFactory.load()

  implicit val system: ActorSystem                        = ActorSystem("sample", config)
  implicit val materializer: ActorMaterializer            = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("slick", config)
  private val profile  = dbConfig.profile
  private val db       = dbConfig.db

  private val environment: AppType = new RDB.Live(profile, db)
  private val design               = DISettings.design(environment)
  private val session              = design.newSession
  session.start

  val controller = session.build[Controller]

  val bindingFuture = Http().bindAndHandle(controller.toRoutes, host, port)

  sys.addShutdownHook {
    session.shutdown
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}