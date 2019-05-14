package usecases

import com.github.j5ik2o.dddbase.{ Aggregate, AggregateSingleWriter, AggregateStringId }
import domain.{ DomainError, DomainValidationResult }
import org.scalatest.FreeSpec
import scalaz.zio.ZIO
import scalaz.zio.internal.{ Platform, PlatformLive }

import scala.reflect._

class UseCaseSpec extends FreeSpec {

  "UseCase" - {
    "definition" - {

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

        trait SampleARepository[F[_]] extends AggregateSingleWriter[F] {
          override type AggregateType = SampleA
          override type IdType        = SampleAID

          def findBy(name: SampleAName): F[Option[SampleA]]
        }
        trait SampleBRepository[F[_]] extends AggregateSingleWriter[F] {
          override type AggregateType = SampleB
          override type IdType        = SampleBID

          def findBy(aId: SampleAID): F[Option[SampleB]]
        }

        case class SampleUseCase[F[_]](
            sampleAIDGenerator: SampleIDGenerator[F, SampleAID],
            sampleARepository: SampleARepository[F],
            sampleBIDGenerator: SampleIDGenerator[F, SampleBID],
            sampleBRepository: SampleBRepository[F]
        ) extends UseCase[F, SampleInputData, SampleOutputData] {
          import cats.implicits._

          override def execute(inputData: SampleInputData)(implicit ME: UseCaseMonadError[F]): F[SampleOutputData] =
            for {
              aName   <- SampleAName.assert(inputData.name).toF
              bDetail <- SampleBDetail.assert(inputData.detail).toF
              maybeA  <- sampleARepository.findBy(aName)
              a <- maybeA.map(ME.pure).getOrElse {
                for {
                  id   <- sampleAIDGenerator.generate
                  newA <- ME.pure(SampleA(id, aName))
                  _    <- sampleARepository.store(newA)
                } yield newA
              }
              maybeB <- sampleBRepository.findBy(a.id)
              b <- maybeB.map(_.reDetail(bDetail)).map(ME.pure).getOrElse {
                sampleBIDGenerator.generate.map { id =>
                  SampleB(id, a.id, bDetail)
                }
              }
              _ <- sampleBRepository.store(b)
            } yield SampleOutputData(a.id.value)
        }
      }

      object SampleInterfacesLayer {
        import SampleDomainLayer._
        import SampleUseCasesLayer._

        trait RDBRecord

        trait RDB[Record <: RDBRecord] {
          val service: RDB.Service[Record]
        }
        object RDB {
          trait Service[Record] {
            def get(id: String): Option[Record]
            def save(id: String, record: Record): Long
            def findBy(f: (String, Record) => Boolean): Option[(String, Record)]
          }
          trait Live[Record <: RDBRecord] extends RDB[Record] {
            private var dummy: scala.collection.mutable.Map[String, Record] = scala.collection.mutable.Map.empty

            override val service: RDB.Service[Record] = new RDB.Service[Record] {

              override def get(id: String): Option[Record] = dummy.get(id)

              override def save(id: String, record: Record): Long = {
                dummy = dummy + (id -> record)
                1L
              }

              override def findBy(f: (String, Record) => Boolean): Option[(String, Record)] = {
                dummy.collectFirst {
                  case (id, record) =>
                    if (f(id, record)) Some(id -> record) else None
                  case _ => None
                }.flatten
              }
            }
          }
        }

        trait KVSRecord

        trait KVS[Record <: KVSRecord] {
          val value: KVS.Service[Record]
        }
        object KVS {
          trait Service[Record] {
            def get(id: String): Option[Record]
            def save(id: String, record: Record): Long
            def findBy(f: (String, Record) => Boolean): Option[(String, Record)]
          }
          trait Live[Record <: KVSRecord] extends KVS[Record] {
            private var dummy: scala.collection.mutable.Map[String, Record] = scala.collection.mutable.Map.empty

            override val value: KVS.Service[Record] = new KVS.Service[Record] {

              override def get(id: String): Option[Record] = dummy.get(id)

              override def save(id: String, record: Record): Long = {
                dummy = dummy + (id -> record)
                1L
              }

              override def findBy(f: (String, Record) => Boolean): Option[(String, Record)] = {
                dummy.collectFirst {
                  case (id, record) =>
                    if (f(id, record)) Some(id -> record) else None
                  case _ => None
                }.flatten
              }
            }
          }
        }

        case class SampleAOnRDBRecord(id: String, name: String) extends RDBRecord

        object SampleARepositoryOnRDBLive
            extends SampleARepository[({ type f[A] = ZIO[RDB[SampleAOnRDBRecord], UseCaseError, A] })#f] {

          override def findBy(name: SampleAName): ZIO[RDB[SampleAOnRDBRecord], UseCaseError, Option[SampleA]] =
            ZIO.access { rdb =>
              rdb.service
                .findBy {
                  case (_, record) =>
                    name.value == record.name
                }.map {
                  case (id, record) =>
                    SampleA(SampleAID(id), SampleAName(record.name))
                }
            }

          override def store(aggregate: SampleA): ZIO[RDB[SampleAOnRDBRecord], UseCaseError, Long] =
            ZIO.access { rdb =>
              rdb.service.save(aggregate.id.value, SampleAOnRDBRecord(aggregate.id.value, aggregate.name.value))
            }
        }

        case class SampleBOnKVSRecord(id: String, aId: String, detail: String) extends KVSRecord

        object SampleBRepositoryOnKVSLive
            extends SampleBRepository[({ type f[A] = ZIO[KVS.Live[SampleBOnKVSRecord], UseCaseError, A] })#f] {
          override def findBy(
              aId: SampleDomainLayer.SampleAID
          ): ZIO[KVS.Live[SampleBOnKVSRecord], UseCaseError, Option[SampleB]] =
            ZIO.access { kvs =>
              kvs.value
                .findBy {
                  case (_, record) =>
                    aId.value == record.aId
                }.map {
                  case (id, record) =>
                    SampleB(SampleBID(id), SampleAID(record.aId), SampleBDetail(record.detail))
                }
            }

          override def store(
              aggregate: SampleDomainLayer.SampleB
          ): ZIO[KVS.Live[SampleBOnKVSRecord], UseCaseError, Long] =
            ZIO.access { kvs =>
              kvs.value.save(
                aggregate.id.value,
                SampleBOnKVSRecord(aggregate.id.value, aggregate.aId.value, aggregate.detail.value)
              )
            }
        }

        trait SamplePresenter[F[_], OutputData, Json] {
          import cats.implicits._

          def response(res: F[OutputData])(implicit ME: UseCaseMonadError[F]): F[Json] =
            res.map((outputData: OutputData) => response(outputData))

          protected def response(outputData: OutputData): Json
        }

        case class SampleResponseJson(id: String)

        trait SamplePresenterImpl[F[_]] extends SamplePresenter[F, SampleOutputData, SampleResponseJson]

        object SampleErrors {
          import cats._
          trait ZIOErrorWrapper[AppType] {
            type FF[A] = ZIO[AppType, UseCaseError, A]
            implicit val useCaseMonadErrorForZIO: MonadError[FF, UseCaseError] =
              new MonadError[FF, UseCaseError] with StackSafeMonad[FF] {
                override def pure[A](x: A): FF[A] = ZIO.succeed(x)

                override def flatMap[A, B](fa: FF[A])(f: A => FF[B]): FF[B] = fa.flatMap(f)

                override def raiseError[A](e: UseCaseError): FF[A] = ZIO.fail(e)

                override def handleErrorWith[A](fa: FF[A])(f: UseCaseError => FF[A]): FF[A] = fa.catchAll(f)

              }
          }
        }
        class SampleController[AppType](
            sampleUseCase: SampleUseCase[({ type f[A]         = ZIO[AppType, UseCaseError, A] })#f],
            samplePresenter: SamplePresenterImpl[({ type f[A] = ZIO[AppType, UseCaseError, A] })#f]
        ) extends SampleErrors.ZIOErrorWrapper[AppType] {

          type ZIOContext[A] = ZIO[AppType, UseCaseError, A]

          def post(name: String, detail: String): ZIOContext[SampleResponseJson] = {
            val inputData = SampleInputData(name, detail)
            samplePresenter.response(sampleUseCase.execute(inputData))
          }
        }

      }

      "execute" in {
        import SampleDomainLayer._
        import SampleInterfacesLayer._
        import SampleUseCasesLayer._

        type AppType       = RDB[SampleAOnRDBRecord] with KVS[SampleBOnKVSRecord]
        type ZIOContext[A] = ZIO[AppType, UseCaseError, A]

        val sampleAIDGenerator = new SampleIDGenerator[ZIOContext, SampleAID] {
          override def generate: ZIOContext[SampleAID] = ZIO.succeed(SampleAID("id-1"))
        }

        val sampleARepository: SampleARepository[ZIOContext] =
          SampleARepositoryOnRDBLive.asInstanceOf[SampleARepository[ZIOContext]]

        val sampleBIDGenerator = new SampleIDGenerator[ZIOContext, SampleBID] {
          override def generate: ZIOContext[SampleBID] = ZIO.succeed(SampleBID("id-1"))
        }

        val sampleBRepository: SampleBRepository[ZIOContext] =
          SampleBRepositoryOnKVSLive.asInstanceOf[SampleBRepository[ZIOContext]]

        val sampleUseCase: SampleUseCase[ZIOContext] =
          SampleUseCase[ZIOContext](sampleAIDGenerator, sampleARepository, sampleBIDGenerator, sampleBRepository)

        val samplePresenter = new SamplePresenterImpl[ZIOContext] {
          override protected def response(outputData: SampleOutputData): SampleResponseJson =
            SampleResponseJson(outputData.id)
        }

        val controller = new SampleController[AppType](sampleUseCase, samplePresenter)

        val runtime = new scalaz.zio.Runtime[AppType] {
          override val Environment: AppType = new RDB.Live[SampleAOnRDBRecord] with KVS.Live[SampleBOnKVSRecord]
          val Platform: Platform            = PlatformLive.Default
        }
        assert(runtime.unsafeRun(controller.post("name", "detail")).id === "id-1")
      }

    }
  }

}
