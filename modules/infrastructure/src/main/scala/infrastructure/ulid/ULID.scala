package infrastructure.ulid

import de.huxhorn.sulky.ulid.ULID.Value
import de.huxhorn.sulky.ulid.{ ULID => ULIDGen }
import eu.timepit.refined.api.{ Refined, Validate }

import scala.util.Try

object ULID {

  private val gen = new ULIDGen()

  def parseFromString(text: String): Try[ULID] = Try {
    apply(ULIDGen.parseULID(text))
  }

  case class ULIDType()

  def apply(value: Value = ULID.gen.nextValue()): ULID = new ULID(value)

  type ULIDStringType = String Refined ULIDType

  implicit def ulidValidate: Validate.Plain[String, ULIDType] =
    Validate.fromPartial(
      s => require(parseFromString(s).isSuccess),
      "ULID",
      ULIDType()
    )
}

final case class ULID private (private val value: Value) extends Ordered[ULID] {

  def timestamp: Long = value.timestamp()
  def increment: ULID = new ULID(value.increment())

  def mostSignificantBits: Long  = value.getMostSignificantBits
  def leastSignificantBits: Long = value.getLeastSignificantBits

  def asString: String     = value.toString
  def asBytes: Array[Byte] = value.toBytes

  override def compare(that: ULID): Int =
    value.compareTo(that.value)

  override def equals(other: Any): Boolean = other match {
    case that: ULID => value == that.value
    case _          => false
  }

  override def hashCode(): Int = {
    val state = Seq(value)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

}
