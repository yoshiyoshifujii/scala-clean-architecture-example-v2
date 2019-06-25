package domain.common

import java.time.ZonedDateTime

case class DateTime(value: ZonedDateTime)

object DateTime {

  def now: DateTime = DateTime(ZonedDateTime.now())

}
