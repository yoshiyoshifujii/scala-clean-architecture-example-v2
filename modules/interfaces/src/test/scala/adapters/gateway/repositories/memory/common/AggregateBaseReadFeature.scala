package adapters.gateway.repositories.memory.common

import adapters.Effect

trait AggregateBaseReadFeature extends AggregateIOBaseFeature {

  protected def convertToAggregate: RecordType => Effect[AggregateType]

}
