package adapters.gateway.repositories.memory.zio.common

import adapters.Effect

trait AggregateBaseReadFeature extends AggregateIOBaseFeature {

  protected def convertToAggregate: RecordType => Effect[AggregateType]

}
