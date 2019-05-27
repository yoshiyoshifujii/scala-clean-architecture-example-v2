package adapters.validator

trait Validator[A, B] {
  def validate(value: A): ValidationResult[B]
}
