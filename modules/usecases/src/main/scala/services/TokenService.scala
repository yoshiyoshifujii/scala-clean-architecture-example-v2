package services

import domain.account.AccountId

trait TokenService[F[_]] {

  def generate(accountId: AccountId): F[String]

  def verify(token: String, acceptExpiresAt: Long): F[AccountId]

}
