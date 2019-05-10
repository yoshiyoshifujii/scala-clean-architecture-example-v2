import cats.data.ValidatedNel

package object domain {

  case class DomainError(message: String)

  type DomainValidationResult[A] = ValidatedNel[DomainError, A]

  private[domain] trait EnumWithValidation[E <: enumeratum.EnumEntry] {
    self: enumeratum.Enum[E] =>
    import cats.implicits._

    def withNameValidation(name: String): DomainValidationResult[E] =
      self.withNameOption(name).map(_.validNel).getOrElse(DomainError(s"$name is not a member").invalidNel)
  }

}
