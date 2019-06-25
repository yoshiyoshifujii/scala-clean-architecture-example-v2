package domain.building

sealed abstract class Pref private[building] (value: String)

object Pref {
  case object Tokyo extends Pref("tokyo")
}
