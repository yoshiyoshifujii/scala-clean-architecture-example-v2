package domain.common

import cats.implicits._
import domain.DomainError
import eu.timepit.refined.W
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.Size
import eu.timepit.refined.numeric.Interval

final case class Email private[common] (value: Email.AsString)

object Email {
  type AsString = String Refined Size[Interval.Closed[W.`1`.T, W.`100`.T]]

  val generate: String => Either[DomainError, Email] = applyRef[AsString](_).leftMap(DomainError).map(new Email(_))
}
