package adapters.gateway.repositories.memory.id.common

import cats.Id

trait AggregateBaseReadFeature extends AggregateIOBaseFeature {

  protected def convertToAggregate: RecordType => Id[AggregateType]

}
