package adapters.gateway.repositories.slick.common

import adapters.Effect

trait AggregateBaseReadFeature extends AggregateIOBaseFeature {

  protected def convertToAggregate: RecordType => Effect[AggregateType]

}
