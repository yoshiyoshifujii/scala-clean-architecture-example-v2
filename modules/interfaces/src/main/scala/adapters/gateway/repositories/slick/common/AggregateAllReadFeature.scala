package adapters.gateway.repositories.slick.common

import adapters.Effect
import com.github.j5ik2o.dddbase.AggregateAllReader
import scalaz.zio.ZIO
import usecases.UseCaseSystemError

trait AggregateAllReadFeature extends AggregateAllReader[Effect] with AggregateBaseReadFeature {

  override def resolveAll: Effect[Seq[AggregateType]] =
    for {
      records <- ZIO
        .fromFuture { implicit ec =>
          import profile.api._
          db.run(dao.result)
        }.foldM(
          cause => ZIO.fail(UseCaseSystemError(cause)),
          ZIO.succeed
        )
      aggregates <- ZIO.collectAll(records.map(convertToAggregate))
    } yield aggregates
}
