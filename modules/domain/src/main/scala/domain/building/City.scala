package domain.building

sealed abstract class City private[building] (value: String)

object City {
  case object Shinagawa extends City("shinagawa")
}
