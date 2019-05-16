package usecases

import org.scalatest.FreeSpec
import scalaz.zio.internal.{ Platform, PlatformLive }

class UseCaseSpec extends FreeSpec {

  "UseCase" - {

    "execute by airframe" in {
      import SampleDomainLayer._
      import SampleInterfacesLayer._
      import SampleUseCasesLayer._
      import wvlet.airframe._

      val design: Design = newDesign
        .bind[SampleIDGenerator[ZIOContext, SampleAID]].to[SampleAIDGenerator]
        .bind[SampleIDGenerator[ZIOContext, SampleBID]].to[SampleBIDGenerator]
        .bind[SampleARepository[ZIOContext]].to[SampleARepositoryOnRDB]
        .bind[SampleBRepository[ZIOContext]].to[SampleBRepositoryOnKVS]
        .bind[SampleUseCase[ZIOContext]].toEagerSingleton

      design.withSession { session =>
        println(session.build[SampleUseCase[ZIOContext]].sampleAIDGenerator.generate)
      }

    }

    "execute" in {
      import SampleDomainLayer._
      import SampleInterfacesLayer._
      import SampleUseCasesLayer._

      val sampleAIDGenerator: SampleIDGenerator[ZIOContext, SampleAID] =
        new SampleAIDGenerator {}

      val sampleARepository: SampleARepository[ZIOContext] =
        new SampleARepositoryOnRDB {}

      val sampleBIDGenerator: SampleIDGenerator[ZIOContext, SampleBID] =
        new SampleBIDGenerator {}

      val sampleBRepository: SampleBRepository[ZIOContext] =
        new SampleBRepositoryOnKVS {}

      val sampleUseCase: SampleUseCase[ZIOContext] =
        SampleUseCase[ZIOContext](sampleAIDGenerator, sampleARepository, sampleBIDGenerator, sampleBRepository)

      val samplePresenter = new SamplePresenterImpl[ZIOContext] {
        override protected def response(outputData: SampleOutputData): SampleResponseJson =
          SampleResponseJson(outputData.id)
      }

      val controller = new SampleController(sampleUseCase, samplePresenter)

      val runtime = new scalaz.zio.Runtime[AppType] {
        override val Environment: AppType = new RDB.Live[SampleAOnRDBRecord] with KVS.Live[SampleBOnKVSRecord]
        val Platform: Platform            = PlatformLive.Default
      }
      assert(runtime.unsafeRun(controller.post("name", "detail")).id === "id-1")
    }

  }

}
