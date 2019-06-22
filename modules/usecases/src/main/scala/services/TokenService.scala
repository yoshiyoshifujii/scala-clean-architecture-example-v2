package services

import domain.account.Auth

trait TokenService[F[_]] {

  def generate(auth: Auth): F[String]

  def verify(token: String, acceptExpiresAt: Long): F[Auth]

}
