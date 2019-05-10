package usecases

import com.github.j5ik2o.dddbase.{ Aggregate, AggregateSingleReader, AggregateSingleWriter, AggregateStringId }
import entities.{ EntitiesError, EntitiesValidationResult }
import org.scalatest.FreeSpec
import scalaz.zio.{ TaskR, ZIO }

import scala.reflect.ClassTag

class SampleUseCaseSpec extends FreeSpec with TestRuntime {

  "sample" - {
    type Input  = String
    type Output = String

    val input: Input = "hoge"

    "success" in {

      val useCase = new UseCase[Any, Input, Output] {
        override def execute(inputData: Input): UseCaseZIOR[Any, Output] =
          ZIO.succeed(s"$inputData-hoge")
      }

      unsafeRun {
        useCase
          .execute(input).fold(
            err => fail(err.toString),
            suc => assert(suc === "hoge-hoge")
          )
      }

    }

    "fail" in {

      val useCase = new UseCase[Any, Input, Output] {
        override def execute(inputData: Input): UseCaseZIOR[Any, Output] =
          ZIO.fail(UseCaseApplicationError(s"$inputData-hoge"))
      }

      unsafeRun {
        useCase
          .execute(input).fold(
            {
              case UseCaseApplicationError(message) => assert(message === "hoge-hoge")
              case err                              => fail(err.toString)
            },
            suc => fail(suc)
          )
      }

    }

  }

  "Sample UseCase and Presenter and Controller" - {

    import cats.implicits._

    trait SampleDatabase {
      def database: SampleDatabase.Service
    }

    object SampleDatabase {
      trait Service
    }

    case class SampleInput(email: String, name: String)
    case class SampleOutput(id: String)

    case class SampleDomainId(value: String) extends AggregateStringId
    case class SampleDomain(id: SampleDomainId, email: String, name: String) extends Aggregate {
      override type IdType        = SampleDomainId
      override type AggregateType = SampleDomain
      override protected val tag: ClassTag[SampleDomain] = scala.reflect.classTag[SampleDomain]

      def renameTo(name: String): EntitiesValidationResult[SampleDomain] =
        if (name.isEmpty) EntitiesError("not empty").invalidNel else this.copy(name = name).validNel
    }
    object SampleDomain {
      def create(email: String, name: String): EntitiesValidationResult[SampleDomain] =
        new SampleDomain(SampleDomainId(java.util.UUID.randomUUID().toString), email = email, name = name).validNel
    }

    trait SampleRepository[F[_]] extends AggregateSingleReader[F] with AggregateSingleWriter[F] {
      override type IdType        = SampleDomainId
      override type AggregateType = SampleDomain

      def findBy(email: String): F[Option[SampleDomain]]
    }

    class SampleUseCaseInteractor[R](
        val sampleRepository: SampleRepository[({ type f[x] = ZIO[R, UseCaseError, x] })#f]
    ) extends UseCase[R, SampleInput, SampleOutput] {
      override def execute(inputData: SampleInput): UseCaseZIOR[R, SampleOutput] =
        for {
          maybe <- sampleRepository.findBy(inputData.email)
          domain <- maybe map { sampleDomain =>
            sampleDomain.renameTo(inputData.name).toUseCaseZIO
          } getOrElse {
            SampleDomain.create(email = inputData.email, name = inputData.name).toUseCaseZIO
          }
          _ <- sampleRepository.store(domain)
        } yield SampleOutput(domain.id.value)
    }

    trait SamplePresenter[R, OutputData, ViewModel] {

      def response(res: ZIO[R, UseCaseError, OutputData]): TaskR[R, ViewModel] =
        res.foldM(
          {
            case UseCaseApplicationError(message) => convertApplicationError(message)
            case UseCaseSystemError(cause)        => TaskR.fail(cause)
          },
          convert
        )

      protected def convertApplicationError(message: String): TaskR[R, ViewModel]
      protected def convert(outputData: OutputData): TaskR[R, ViewModel]

    }

    trait SampleController[InputData, OutputData, ViewModel] {
      type R
      val useCase: UseCase[R, InputData, OutputData]
      val presenter: SamplePresenter[R, OutputData, ViewModel]

      protected def execute(inputData: InputData): TaskR[R, ViewModel] =
        presenter.response(useCase.execute(inputData))
    }

//    class SampleControllerImpl() extends SampleController[]

  }

}
