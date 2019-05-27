package adapters.gateway.repositories.slick

import adapters.Effect
import domain.account.ResolvedAccount
import domain.common.Email
import scalaz.zio.ZIO
import slick.jdbc.JdbcProfile

class AccountRepositoryBySlick(override val profile: JdbcProfile, override val db: JdbcProfile#Backend#Database)
    extends AbstractAccountRepositoryBySlick(profile, db) {

  override def findBy(email: Email): Effect[Option[ResolvedAccount]] = ZIO.succeed(None)

}
