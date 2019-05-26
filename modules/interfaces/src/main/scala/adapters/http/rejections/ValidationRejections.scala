package adapters.http.rejections

import akka.http.javadsl.server.CustomRejection
import cats.data.NonEmptyList
import domain.DomainError

case class ValidationRejections(errors: NonEmptyList[DomainError]) extends CustomRejection {
  val message: String            = errors.toList.map(_.message).mkString(",")
  val cause: Option[DomainError] = None
  protected def withCauseMessage = s"$message${cause.fold("")(v => s": ${v.message}")}"
}
