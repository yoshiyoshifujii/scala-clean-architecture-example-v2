package adapters.gateway.repositories

import adapters.Effect
import domain.account.{ Account, AccountId, ResolvedAccount }
import domain.common.Email
import repositories.AccountRepository
import scalaz.zio.ZIO

trait AccountRepositoryOnRDB extends AccountRepository[Effect] {

  override def findBy(email: Email): Effect[Option[ResolvedAccount]] = ZIO.succeed(None)

  override def store(aggregate: Account): Effect[Long] = ZIO.succeed(1L)

  override def resolveById(id: AccountId): Effect[Account] = ???
}
