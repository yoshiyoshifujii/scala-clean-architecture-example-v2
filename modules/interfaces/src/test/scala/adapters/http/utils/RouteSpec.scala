package adapters.http.utils

import adapters.{ AppType, DISettings }
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.testkit.TestKit
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalatest.{ BeforeAndAfterAll, Matchers, TestSuite }
import wvlet.airframe.{ newDesign, Design, Session }

import scala.concurrent.duration._

trait RouteSpec extends ScalatestRouteTest with Matchers with BeforeAndAfterAll with FailFastCirceSupport {
  this: TestSuite =>

  implicit def timeout: RouteTestTimeout = RouteTestTimeout(5 seconds)
//  implicit val context                   = Context()
  private var _session: Session = _
  def session: Session          = _session

  def environment: AppType

  def design: Design =
    newDesign
      .add(DISettings.designOfRuntime(environment))
      .add(DISettings.designOfHttpControllers)

  override def beforeAll(): Unit = {
    super.beforeAll()
    _session = design.newSession
    _session.start
  }

  override def afterAll(): Unit = {
    _session.shutdown
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

}
