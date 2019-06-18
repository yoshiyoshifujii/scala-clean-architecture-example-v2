package adapters.gateway.repositories.memory.zio.common

import adapters.Effect

trait AggregateBaseWriteFeature extends AggregateIOBaseFeature {

  protected def convertToRecord: AggregateType => Effect[RecordType]

}
