package adapters.gateway.repositories.slick.common

import adapters.{ AppType, Effect }
import com.github.j5ik2o.dddbase.{ AggregateSingleHardDeletable, AggregateSingleWriter }
import zio.ZIO
import usecases.{ UseCaseError, UseCaseSystemError }

trait AggregateSingleHardDeleteFeature extends AggregateSingleHardDeletable[Effect] with AggregateBaseWriteFeature {
  this: AggregateSingleWriter[Effect] =>

  override def hardDelete(id: IdType): Effect[Long] =
    ZIO
      .fromFuture { implicit ec =>
        import profile.api._
        db.run(dao.filter(byCondition(id)).delete.map(_.toLong))
      }.foldM[AppType, UseCaseError, Long](
        cause => ZIO.fail(UseCaseSystemError(cause)),
        ZIO.succeed
      )
}
