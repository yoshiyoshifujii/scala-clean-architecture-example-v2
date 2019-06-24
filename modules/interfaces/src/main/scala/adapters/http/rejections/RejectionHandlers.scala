package adapters.http.rejections

import adapters.http.json.ErrorResponseJson
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.RejectionHandler
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

object RejectionHandlers {

  final val default: RejectionHandler = RejectionHandler
    .newBuilder()
    .handle {
      case ValidationRejection(errors) =>
        complete((StatusCodes.BadRequest, ErrorResponseJson(errors.map(_.message).toList)))
    }
    .result()
}
