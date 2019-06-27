package usecases

import com.github.j5ik2o.dddbase.{ Aggregate, AggregateSingleWriter, AggregateStringId }
import domain.{ DomainError, DomainValidationResult }
import zio.ZIO

import scala.reflect.{ classTag, ClassTag }

object SampleDomainLayer {
  trait SampleAssertBase {
    import cats.implicits._
    protected def assertNonEmpty(value: String): DomainValidationResult[String] =
      if (value.nonEmpty) value.validNel else DomainError("kvsService is empty").invalidNel
    protected def assertMaxLength(value: String, length: Int): DomainValidationResult[String] =
      if (value.length <= length) value.validNel else DomainError("kvsService is over max length").invalidNel

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
  trait SampleAIDGenerator[F[_]] extends SampleIDGenerator[F, SampleAID]
  trait SampleBIDGenerator[F[_]] extends SampleIDGenerator[F, SampleBID]

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

  class SampleUseCase[F[_]](
      sampleAIDGenerator: SampleAIDGenerator[F],
      sampleARepository: SampleARepository[F],
      sampleBIDGenerator: SampleBIDGenerator[F],
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

object SampleInterfacesContractLayer {
  import SampleUseCasesLayer._

  case class SampleResponseJson(id: String)

  trait SamplePresenter[F[_], OutputData, ViewModel] {
    import cats.implicits._

    def response(res: F[OutputData])(implicit ME: UseCaseMonadError[F]): F[ViewModel] =
      res.map((outputData: OutputData) => response(outputData))

    protected def response(outputData: OutputData): ViewModel
  }

  class SampleController[F[_], ViewModel](
      sampleUseCase: SampleUseCase[F],
      samplePresenter: SamplePresenter[F, SampleOutputData, ViewModel]
  ) {

    def post(name: String, detail: String)(implicit ME: UseCaseMonadError[F]): F[ViewModel] = {
      val inputData = SampleInputData(name, detail)
      samplePresenter.response(sampleUseCase.execute(inputData))
    }
  }

}

object SampleInterfacesLayer {
  import SampleDomainLayer._
  import SampleUseCasesLayer._
  import SampleInterfacesContractLayer._

  trait RDBRecord
  case class SampleAOnRDBRecord(id: String, name: String) extends RDBRecord

  trait RDB[Record <: RDBRecord] {
    val rdbService: RDB.Service[Record]
  }
  object RDB {
    trait Service[Record] {
      def get(id: String): Option[Record]
      def save(id: String, record: Record): Long
      def findBy(f: (String, Record) => Boolean): Option[(String, Record)]
    }
    trait Live[Record <: RDBRecord] extends RDB[Record] {
      private var dummy: scala.collection.mutable.Map[String, Record] = scala.collection.mutable.Map.empty

      override val rdbService: RDB.Service[Record] = new RDB.Service[Record] {

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
  case class SampleBOnKVSRecord(id: String, aId: String, detail: String) extends KVSRecord

  trait KVS[Record <: KVSRecord] {
    val kvsService: KVS.Service[Record]
  }
  object KVS {
    trait Service[Record] {
      def get(id: String): Option[Record]
      def save(id: String, record: Record): Long
      def findBy(f: (String, Record) => Boolean): Option[(String, Record)]
    }
    trait Live[Record <: KVSRecord] extends KVS[Record] {
      private var dummy: scala.collection.mutable.Map[String, Record] = scala.collection.mutable.Map.empty

      override val kvsService: KVS.Service[Record] = new KVS.Service[Record] {

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

  type AppType       = RDB[SampleAOnRDBRecord] with KVS[SampleBOnKVSRecord]
  type ZIOContext[A] = ZIO[AppType, UseCaseError, A]

  trait SampleARepositoryOnRDB extends SampleARepository[ZIOContext] {
    override def findBy(name: SampleAName): ZIOContext[Option[SampleA]] =
      ZIO.access { appType =>
        appType.rdbService
          .findBy {
            case (_, record) =>
              name.value == record.name
          }.map {
            case (id, record) =>
              SampleA(SampleAID(id), SampleAName(record.name))
          }
      }

    override def store(aggregate: SampleA): ZIOContext[Long] =
      ZIO.access { appType =>
        appType.rdbService.save(aggregate.id.value, SampleAOnRDBRecord(aggregate.id.value, aggregate.name.value))
      }
  }

  trait SampleBRepositoryOnKVS extends SampleBRepository[ZIOContext] {

    override def findBy(aId: SampleAID): SampleInterfacesLayer.ZIOContext[Option[SampleB]] =
      ZIO.access { appType =>
        appType.kvsService
          .findBy {
            case (_, record) =>
              aId.value == record.aId
          }.map {
            case (id, record) =>
              SampleB(SampleBID(id), SampleAID(record.aId), SampleBDetail(record.detail))
          }
      }

    override def store(aggregate: SampleB): ZIOContext[Long] =
      ZIO.access { kvs =>
        kvs.kvsService.save(
          aggregate.id.value,
          SampleBOnKVSRecord(aggregate.id.value, aggregate.aId.value, aggregate.detail.value)
        )
      }

  }

  trait SampleAIDGeneratorImpl extends SampleAIDGenerator[ZIOContext] {
    override def generate: ZIOContext[SampleAID] = ZIO.succeed(SampleAID("id-1"))
  }

  trait SampleBIDGeneratorImpl extends SampleBIDGenerator[ZIOContext] {
    override def generate: ZIOContext[SampleBID] = ZIO.succeed(SampleBID("id-1"))
  }

  trait SamplePresenterImpl extends SamplePresenter[ZIOContext, SampleOutputData, SampleResponseJson] {
    override protected def response(outputData: SampleOutputData): SampleResponseJson =
      SampleResponseJson(outputData.id)
  }

  object SampleErrors {
    import cats._
    implicit val useCaseMonadErrorForZIO: MonadError[ZIOContext, UseCaseError] =
      new MonadError[ZIOContext, UseCaseError] with StackSafeMonad[ZIOContext] {
        override def pure[A](x: A): ZIOContext[A] = ZIO.succeed(x)

        override def flatMap[A, B](fa: ZIOContext[A])(f: A => ZIOContext[B]): ZIOContext[B] = fa.flatMap(f)

        override def raiseError[A](e: UseCaseError): ZIOContext[A] = ZIO.fail(e)

        override def handleErrorWith[A](fa: ZIOContext[A])(f: UseCaseError => ZIOContext[A]): ZIOContext[A] =
          fa.catchAll(f)

      }

    implicit val useCasesMonadErrorForId: MonadError[Id, UseCaseError] =
      new MonadError[Id, UseCaseError] with StackSafeMonad[Id] {
        override def pure[A](x: A): Id[A] = x

        override def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] = f(fa)

        override def raiseError[A](e: UseCaseError): Id[A] = throw new Exception(e.toString)

        override def handleErrorWith[A](fa: Id[A])(f: UseCaseError => Id[A]): Id[A] = ???

      }
  }

}
