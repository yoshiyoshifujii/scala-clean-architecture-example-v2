import sbt._

object ScalaTest {
  val version = "3.0.5"
  val core    = "org.scalatest" %% "scalatest" % version
}

object Passay {
  val version = "1.3.0"
  val passay  = "org.passay" % "passay" % version
}

object Commons {
  val version = "1.11"
  val codec   = "commons-codec" % "commons-codec" % version
}

object Scalaz {
  val version = "1.0-RC4"
  val zio     = "org.scalaz" %% "scalaz-zio" % version
}

object ScalaDDDBase {
  val version = "1.0.27"
  val core    = "com.github.j5ik2o" %% "scala-ddd-base-core" % version
  val memory  = "com.github.j5ik2o" %% "scala-ddd-base-memory" % version
}

object Cats {
  val version = "1.6.0"
  val core    = "org.typelevel" %% "cats-core" % version
  val free    = "org.typelevel" %% "cats-free" % version
}

object Beachape {
  val version    = "1.5.13"
  val enumeratum = "com.beachape" %% "enumeratum" % version
}

object Wvlet {
  val version  = "19.5.0"
  val airframe = "org.wvlet.airframe" %% "airframe" % version
}

object Timepit {
  val version = "0.9.5"
  val refined = "eu.timepit" %% "refined" % version
}

object Akka {
  val version = "2.5.19"
  val stream  = "com.typesafe.akka" %% "akka-stream" % version
  val testkit = "com.typesafe.akka" %% "akka-testkit" % version
}

object AkkaHttp {
  val version = "10.1.8"
  val http    = "com.typesafe.akka" %% "akka-http" % version
  val testkit = "com.typesafe.akka" %% "akka-http-testkit" % version
}

object Heikoseeberger {
  val version = "1.25.2"
  val circe   = "de.heikoseeberger" %% "akka-http-circe" % version
}

object Circe {
  val version = "0.11.1"
  val core    = "io.circe" %% "circe-core" % version
  val generic = "io.circe" %% "circe-generic" % version
  val parser  = "io.circe" %% "circe-parser" % version
}

object Slf4j {
  val version = "1.7.25"
  val api     = "org.slf4j" % "slf4j-api" % version
}

object Huxhorn {
  val version = "8.2.0"
  val ulid    = "de.huxhorn.sulky" % "de.huxhorn.sulky.ulid" % version
}

object MySQL {
  val version   = "5.1.42"
  val connector = "mysql" % "mysql-connector-java" % version
}

object Slick {
  val version  = "3.2.3"
  val slick    = "com.typesafe.slick" %% "slick" % version
  val hikaricp = "com.typesafe.slick" %% "slick-hikaricp" % version
}

object Debasishg {
  val version     = "3.9"
  val redisclient = "net.debasishg" %% "redisclient" % version
}

object T3hnar {
  val version = "4.0"
  val bCrypt  = "com.github.t3hnar" %% "scala-bcrypt" % version
}

object Google {
  val version = "27.1-jre"
  val guava   = "com.google.guava"   % "guava" % version
}
