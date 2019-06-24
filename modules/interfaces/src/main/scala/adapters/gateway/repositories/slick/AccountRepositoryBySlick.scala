package adapters.gateway.repositories.slick

import adapters.Effect
import domain.common.Email
import scalaz.zio.ZIO
import slick.jdbc.JdbcProfile
import usecases.UseCaseSystemError

class AccountRepositoryBySlick(override val profile: JdbcProfile, override val db: JdbcProfile#Backend#Database)
    extends AbstractAccountRepositoryBySlick(profile, db) {

  override def findBy(email: Email): Effect[Option[AggregateType]] =
    for {
      record <- ZIO
        .fromFuture { implicit ec =>
          import profile.api._
          db.run(dao.filter(_.email === email.value.value).take(1).result)
            .map(_.headOption)
        }.foldM(
          cause => ZIO.fail(UseCaseSystemError(cause)),
          ZIO.succeed
        )
      aggregate <- record match {
        case Some(r) => convertToAggregate(r).map(Some(_))
        case None    => ZIO.succeed(None)
      }
    } yield aggregate

}
