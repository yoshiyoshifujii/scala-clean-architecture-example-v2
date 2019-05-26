package adapters.gateway.repositories

import adapters.Effect
import domain.account.{ Account, AccountId, ResolvedAccount }
import domain.common.Email
import repositories.AccountRepository

trait AccountRepositoryOnRDB extends AccountRepository[Effect] {

  override def findBy(email: Email): Effect[Option[ResolvedAccount]] = ???

  override def store(aggregate: Account): Effect[Long] = ???

  override def resolveById(id: AccountId): Effect[Account] = ???
}
