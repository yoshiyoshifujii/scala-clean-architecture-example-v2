package adapters.gateway.repositories.memory.id.common

import cats.Id

trait AggregateBaseWriteFeature extends AggregateIOBaseFeature {

  protected def convertToRecord: AggregateType => Id[RecordType]

}
