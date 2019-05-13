package usecases

import com.github.j5ik2o.dddbase.{ Aggregate, AggregateSingleReader, AggregateSingleWriter, AggregateStringId }
import domain.{ DomainError, DomainValidationResult }
import org.scalatest.FreeSpec

import scala.reflect._

class UseCaseSpec extends FreeSpec {

  "UseCase" - {
    "execute" in {

      object SampleDomainLayer {
        trait SampleAssertBase {
          import cats.implicits._
          protected def assertNonEmpty(value: String): DomainValidationResult[String] =
            if (value.nonEmpty) value.validNel else DomainError("value is empty").invalidNel
          protected def assertMaxLength(value: String, length: Int): DomainValidationResult[String] =
            if (value.length <= length) value.validNel else DomainError("value is over max length").invalidNel

        }

        case class SampleAID(value: String) extends AggregateStringId
        case class SampleAName(value: String) {
          assert(value.nonEmpty)
          assert(value.length <= 14)
        }
        object SampleAName extends SampleAssertBase {
          import cats.implicits._
          def assert(value: String): DomainValidationResult[SampleAName] =
            (assertNonEmpty(value), assertMaxLength(value, 14)).mapN { (_, _) =>
              SampleAName(value)
            }
        }
        case class SampleA(id: SampleAID, name: SampleAName) extends Aggregate {
          override type AggregateType = SampleA
          override type IdType        = SampleAID
          override protected val tag: ClassTag[SampleA] = classTag[SampleA]
        }

        case class SampleBID(value: String) extends AggregateStringId
        case class SampleBDetail(value: String) {
          assert(value.nonEmpty)
          assert(value.length <= 100)
        }
        object SampleBDetail extends SampleAssertBase {
          import cats.implicits._
          def assert(value: String): DomainValidationResult[SampleBDetail] =
            (assertNonEmpty(value), assertMaxLength(value, 100)).mapN { (_, _) =>
              SampleBDetail(value)
            }
        }
        case class SampleB(id: SampleBID, aId: SampleAID, detail: SampleBDetail) extends Aggregate {
          override type AggregateType = SampleB
          override type IdType        = SampleBID
          override protected val tag: ClassTag[SampleB] = classTag[SampleB]

          def reDetail(detail: SampleBDetail): SampleB =
            this.copy(detail = detail)
        }
      }

      object SampleUseCasesLayer {
        import SampleDomainLayer._

        case class SampleInputData(name: String, detail: String)
        case class SampleOutputData(id: String)

        trait SampleIDGenerator[F[_], A] {
          def generate: F[A]
        }

        trait SampleARepository[F[_]] extends AggregateSingleReader[F] with AggregateSingleWriter[F] {
          override type AggregateType = SampleA
          override type IdType        = SampleAID

          def findBy(name: SampleAName): F[Option[SampleA]]
        }
        trait SampleBRepository[F[_]] extends AggregateSingleReader[F] with AggregateSingleWriter[F] {
          override type AggregateType = SampleB
          override type IdType        = SampleBID

          def findBy(aId: SampleAID): F[Option[SampleB]]
        }

        class SampleUseCase[F[_]](
            sampleAIDGenerator: SampleIDGenerator[F, SampleAID],
            sampleARepository: SampleARepository[F],
            sampleBIDGenerator: SampleIDGenerator[F, SampleBID],
            sampleBRepository: SampleBRepository[F]
        )(implicit ME: UseCaseMonadError[F])
            extends UseCase[F, SampleInputData, SampleOutputData] {
          import cats.implicits._

          override def execute(inputData: SampleInputData): F[SampleOutputData] =
            for {
              aName   <- SampleAName.assert(inputData.name).toF
              bDetail <- SampleBDetail.assert(inputData.detail).toF
              maybeA  <- sampleARepository.findBy(aName)
              a <- maybeA.map(ME.pure).getOrElse {
                sampleAIDGenerator.generate.map { id =>
                  SampleA(id, aName)
                }
              }
              maybeB <- sampleBRepository.findBy(a.id)
              b <- maybeB.map(_.reDetail(bDetail)).map(ME.pure).getOrElse {
                sampleBIDGenerator.generate.map { id =>
                  SampleB(id, a.id, bDetail)
                }
              }
            } yield SampleOutputData(a.id.value)
        }
      }

      object SampleInterfacesLayer {

        object SampleGatewayLayer {
          trait RDB {
            val rdb: RDB.Service
          }
          object RDB {
            trait Service
          }
          trait KVS {
            val kvs: KVS.Service
          }
          object KVS {
            trait Service
          }
        }

      }

//      type AppType    = RDB with KVS
//      type Context[A] = ZIO[AppType, UseCaseError, A]

    }
  }

}
