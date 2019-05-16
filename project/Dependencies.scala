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
  val codec = "commons-codec" % "commons-codec" % version
}

object Scalaz {
  val version = "1.0-RC4"
  val zio = "org.scalaz" %% "scalaz-zio" % version
}

object ScalaDDDBase {
  val version = "1.0.26"
  val core    = "com.github.j5ik2o" %% "scala-ddd-base-core" % version
}

object Cats {
  val version = "1.6.0"
  val core = "org.typelevel" %% "cats-core"  % version
  val free = "org.typelevel" %% "cats-free"  % version
}

object Beachape {
  val version = "1.5.13"
  val enumeratum = "com.beachape"  %% "enumeratum" % version
}

object Wvlet {
  val version = "19.5.0"
  val airframe = "org.wvlet.airframe" %% "airframe" % version
}
