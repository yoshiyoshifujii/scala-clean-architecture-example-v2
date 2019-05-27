package adapters.gateway.services

import adapters.Effect
import scalaz.zio.ZIO
import services.EncryptService

trait EncryptServiceOnBCrypt extends EncryptService[Effect] {

  override def encrypt(value: String): Effect[String] = ZIO.succeed("hoge")

  override def matches(value0: String, value1: String): Effect[Boolean] = ???
}
