package adapters.gateway.repositories.memory.zio.common

import adapters.Effect
import com.github.j5ik2o.dddbase.{ AggregateNotFoundException, AggregateSingleReader }
import scalaz.zio.ZIO
import usecases.UseCaseSystemError

trait AggregateSingleReadFeature extends AggregateSingleReader[Effect] with AggregateBaseReadFeature {

  override def resolveById(id: IdType): Effect[AggregateType] =
    for {
      record <- dao.get(id.value.toString).flatMap {
        case Some(v) => ZIO.succeed(v)
        case None    => ZIO.fail(UseCaseSystemError(AggregateNotFoundException(id)))
      }
      aggregate <- convertToAggregate(record)
    } yield aggregate

}
