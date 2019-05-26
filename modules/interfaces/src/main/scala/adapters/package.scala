import scalaz.zio.ZIO
import usecases.UseCaseError

package object adapters {

  type AppType   = String
  type Effect[A] = ZIO[AppType, UseCaseError, A]

}
