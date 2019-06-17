package adapters.gateway.repositories.slick

import adapters.Effect
import domain.account.Account
import domain.common.Email
import scalaz.zio.ZIO
import slick.jdbc.JdbcProfile
import usecases.UseCaseSystemError

class AccountRepositoryBySlick(override val profile: JdbcProfile, override val db: JdbcProfile#Backend#Database)
    extends AbstractAccountRepositoryBySlick(profile, db) {

  override def store(aggregate: Account): Effect[Long] =
    for {
      record <- convertToRecord(aggregate)
      result <- ZIO
        .fromFuture { implicit ec =>
          import profile.api._
          val action: DBIOAction[Long, NoStream, Effect.Read with Effect.Write] = for {
            maybe <- dao.filter(_.email === aggregate.email.value.value).take(1).result.headOption
            result <- maybe match {
              case Some(_) => slick.dbio.DBIOAction.failed(new RuntimeException("already exists."))
              case None    => dao.insertOrUpdate(record).map(_.toLong)
            }
          } yield result
          db.run(action.transactionally)
        }.foldM(
          cause => ZIO.fail(UseCaseSystemError(cause)),
          ZIO.succeed
        )
    } yield result

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
