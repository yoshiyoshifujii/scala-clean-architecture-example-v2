import adapters.dao.jdbc.RDB
import zio.ZIO
import usecases.UseCaseError

package object adapters {

  type AppType   = RDB
  type Effect[A] = ZIO[AppType, UseCaseError, A]

}
