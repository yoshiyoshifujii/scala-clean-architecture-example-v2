package adapters.gateway.repositories.slick.common

import adapters.Effect
import com.github.j5ik2o.dddbase.{ AggregateNotFoundException, AggregateSingleReader }
import scalaz.zio.ZIO
import usecases.UseCaseSystemError

trait AggregateSingleReadFeature extends AggregateSingleReader[Effect] with AggregateBaseReadFeature {

  override def resolveById(id: IdType): Effect[AggregateType] =
    for {
      record <- ZIO
        .fromFuture { implicit ec =>
          import profile.api._
          db.run(dao.filter(byCondition(id)).take(1).result)
            .map(_.headOption)
            .map(_.getOrElse(throw AggregateNotFoundException(id)))
        }.foldM(
          cause => ZIO.fail(UseCaseSystemError(cause)),
          ZIO.succeed
        )
      aggregate <- convertToAggregate(record)
    } yield aggregate
}
