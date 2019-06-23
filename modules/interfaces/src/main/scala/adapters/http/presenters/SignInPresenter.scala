package adapters.http.presenters

import adapters.http.json.SignInResponseJson
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import usecases.anonymous.SignInOutput

trait SignInPresenter extends Presenter[SignInOutput] {

  override protected def convert(outputData: SignInOutput): Route =
    complete(SignInResponseJson(Some(outputData.token)))

}
