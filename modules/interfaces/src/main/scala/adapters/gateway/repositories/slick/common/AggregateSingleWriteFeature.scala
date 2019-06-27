package adapters.gateway.repositories.slick.common

import adapters.Effect
import com.github.j5ik2o.dddbase.AggregateSingleWriter
import zio.ZIO
import usecases.UseCaseSystemError

trait AggregateSingleWriteFeature extends AggregateSingleWriter[Effect] with AggregateBaseWriteFeature {

  override def store(aggregate: AggregateType): Effect[Long] =
    for {
      record <- convertToRecord(aggregate)
      result <- ZIO
        .fromFuture { implicit ec =>
          import profile.api._
          db.run(dao.insertOrUpdate(record)).map(_.toLong)
        }.foldM(
          cause => ZIO.fail(UseCaseSystemError(cause)),
          ZIO.succeed
        )
    } yield result
}
