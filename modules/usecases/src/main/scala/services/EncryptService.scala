package services

trait EncryptService[F[_]] {

  def encrypt(value: String): F[String]

  def matches(value0: String, value1: String): F[Boolean]

}
