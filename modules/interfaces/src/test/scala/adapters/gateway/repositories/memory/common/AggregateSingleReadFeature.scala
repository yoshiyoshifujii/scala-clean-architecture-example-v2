package adapters.gateway.repositories.memory.common

import adapters.Effect
import com.github.j5ik2o.dddbase.AggregateSingleReader
import scalaz.zio.ZIO
import usecases.UseCaseApplicationError

trait AggregateSingleReadFeature extends AggregateSingleReader[Effect] with AggregateBaseReadFeature {

  override def resolveById(id: IdType): Effect[AggregateType] =
    for {
      record <- dao.get(id.value.toString).flatMap {
        case Some(v) => ZIO.succeed(v)
        case None    => ZIO.fail(UseCaseApplicationError(s"Not Found Error. $id"))
      }
      aggregate <- convertToAggregate(record)
    } yield aggregate

}
