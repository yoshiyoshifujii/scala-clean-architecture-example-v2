import Settings._

val baseName = "scala-clean-architecture-example-v2"

lazy val `infrastructure` = (project in file("modules/infrastructure"))
  .settings(
    name := s"$baseName-infrastructure",
    libraryDependencies ++= Seq(
      Passay.passay,
      Commons.codec
    )
  )
  .settings(coreSettings)

lazy val `domain` = (project in file("modules/domain"))
  .settings(
    name := s"$baseName-domain",
    libraryDependencies ++= Seq(
      ScalaDDDBase.core,
      Cats.core,
      Cats.free,
      Beachape.enumeratum,
      Timepit.refined
    )
  )
  .settings(coreSettings)
  .dependsOn(infrastructure)

lazy val `usecases` = (project in file("modules/usecases"))
  .settings(
    name := s"$baseName-usecases",
    libraryDependencies ++= Seq(
      Scalaz.zio % Test,
      Wvlet.airframe % Test
    )
  )
  .settings(coreSettings)
  .dependsOn(`domain`)

lazy val `interfaces` = (project in file("modules/interfaces"))
  .settings(
    name := s"$baseName-interfaces",
    libraryDependencies ++= Seq()
  )
  .settings(coreSettings)
  .dependsOn(`usecases`)

lazy val `applications-http` = (project in file("applications/http"))
  .settings(
    name := s"$baseName-applications-http"
  )
  .settings(coreSettings)
  .dependsOn(`interfaces`)

lazy val `root` = (project in file("."))
  .settings(
    name := baseName
  )
  .settings(coreSettings)
  .aggregate(
    `infrastructure`,
    `domain`,
    `usecases`,
    `interfaces`,
    `applications-http`
  )

