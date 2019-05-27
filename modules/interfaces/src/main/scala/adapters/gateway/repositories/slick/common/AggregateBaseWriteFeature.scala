package adapters.gateway.repositories.slick.common

import adapters.Effect

trait AggregateBaseWriteFeature extends AggregateIOBaseFeature {

  protected def convertToRecord: AggregateType => Effect[RecordType]

}
