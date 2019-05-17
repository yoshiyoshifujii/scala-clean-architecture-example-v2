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
        .bind[SamplePresenter[ZIOContext, SampleOutputData, SampleResponseJson]].to[SamplePresenterImpl]
        .bind[SampleController[ZIOContext]].toEagerSingleton

      design.withSession { session =>
        val runtime = new scalaz.zio.Runtime[AppType] {
          override val Environment: AppType = new RDB.Live[SampleAOnRDBRecord] with KVS.Live[SampleBOnKVSRecord]
          val Platform: Platform            = PlatformLive.Default
        }
        import SampleErrors._
        assert(runtime.unsafeRun(session.build[SampleController[ZIOContext]].post("name", "detail")).id === "id-1")
      }

    }

    "execute" in {
      import SampleInterfacesLayer._
      import SampleUseCasesLayer._

      val sampleAIDGenerator: SampleAIDGenerator = new SampleAIDGeneratorImpl {}

      val sampleARepository: SampleARepository[ZIOContext] =
        new SampleARepositoryOnRDB {}

      val sampleBIDGenerator: SampleBIDGenerator = new SampleBIDGeneratorImpl {}

      val sampleBRepository: SampleBRepository[ZIOContext] =
        new SampleBRepositoryOnKVS {}

      val sampleUseCase: SampleUseCase[ZIOContext] =
        new SampleUseCase[ZIOContext](sampleAIDGenerator, sampleARepository, sampleBIDGenerator, sampleBRepository)

      val samplePresenter: SamplePresenter[ZIOContext, SampleOutputData, SampleResponseJson] =
        new SamplePresenterImpl {}

      val controller = new SampleController(sampleUseCase, samplePresenter)

      val runtime = new scalaz.zio.Runtime[AppType] {
        override val Environment: AppType = new RDB.Live[SampleAOnRDBRecord] with KVS.Live[SampleBOnKVSRecord]
        val Platform: Platform            = PlatformLive.Default
      }
      import SampleErrors._
      assert(runtime.unsafeRun(controller.post("name", "detail")).id === "id-1")
    }

  }

}
