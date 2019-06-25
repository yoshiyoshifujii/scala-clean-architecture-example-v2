package domain.building

sealed abstract class Country private[building] (value: String)

object Country {
  case object Japan extends Country("japan")
}
