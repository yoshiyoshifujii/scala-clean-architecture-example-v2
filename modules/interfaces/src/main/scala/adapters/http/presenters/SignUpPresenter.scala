package adapters.http.presenters

import adapters.http.json.SignUpResponseJson
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import usecases.anonymous.SignUpOutput

trait SignUpPresenter extends Presenter[SignUpOutput] {

  override protected def convert(outputData: SignUpOutput): Route =
    complete(SignUpResponseJson(Some(outputData.id.value)))

}
