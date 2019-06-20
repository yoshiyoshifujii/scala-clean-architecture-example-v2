package services

trait TokenService[F[_]] {

  def generate: F[String]

}
