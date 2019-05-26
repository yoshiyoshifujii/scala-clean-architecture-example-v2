package adapters

import domain.DomainValidationResult

package object validator {
  type ValidationResult[A] = DomainValidationResult[A]
}
