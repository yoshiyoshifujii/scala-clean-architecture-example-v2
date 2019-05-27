package usecases

import org.scalatest.FreeSpec
import scalaz.zio.internal.{ Platform, PlatformLive }

class UseCaseSpec extends FreeSpec {

  "UseCase" - {

    "execute by airframe" in {
      import SampleUseCasesLayer._
      import SampleInterfacesContractLayer._
      import SampleInterfacesLayer._
      import wvlet.airframe._

      val design: Design = newDesign
        .bind[SampleAIDGenerator[ZIOContext]].to[SampleAIDGeneratorImpl]
        .bind[SampleBIDGenerator[ZIOContext]].to[SampleBIDGeneratorImpl]
        .bind[SampleARepository[ZIOContext]].to[SampleARepositoryOnRDB]
        .bind[SampleBRepository[ZIOContext]].to[SampleBRepositoryOnKVS]
        .bind[SampleUseCase[ZIOContext]].toEagerSingleton
        .bind[SamplePresenter[ZIOContext, SampleOutputData, SampleResponseJson]].to[SamplePresenterImpl]
        .bind[SampleController[ZIOContext, SampleResponseJson]].toEagerSingleton

      design.withSession { session =>
        val runtime = new scalaz.zio.Runtime[AppType] {
          override val Environment: AppType = new RDB.Live[SampleAOnRDBRecord] with KVS.Live[SampleBOnKVSRecord]
          val Platform: Platform            = PlatformLive.Default
        }
        import SampleErrors._
        assert(
          runtime
            .unsafeRun(session.build[SampleController[ZIOContext, SampleResponseJson]].post("name", "detail")).id === "id-1"
        )
      }

    }

    "execute" in {
      import SampleUseCasesLayer._
      import SampleInterfacesContractLayer._
      import SampleInterfacesLayer._

      val sampleAIDGenerator: SampleAIDGenerator[ZIOContext] = new SampleAIDGeneratorImpl {}
      val sampleARepository: SampleARepository[ZIOContext]   = new SampleARepositoryOnRDB {}
      val sampleBIDGenerator: SampleBIDGenerator[ZIOContext] = new SampleBIDGeneratorImpl {}
      val sampleBRepository: SampleBRepository[ZIOContext]   = new SampleBRepositoryOnKVS {}

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
