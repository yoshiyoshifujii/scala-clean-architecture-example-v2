import org.scalafmt.sbt.ScalafmtPlugin.autoImport._
import sbt.Keys._
import sbt._

object Settings {

  val coreSettings: Def.SettingsDefinition = Seq(
    organization := "com.github.yoshiyoshiifujii",
    scalaVersion := "2.12.8",
    scalacOptions ++= {
      Seq(
        "-feature",
        "-deprecation",
        "-unchecked",
        "-encoding",
        "UTF-8",
        "-Xfatal-warnings",
        "-language:_",
        // Warn if an argument list is modified to match the receiver
        "-Ywarn-adapted-args",
        // Warn when dead code is identified.
        "-Ywarn-dead-code",
        // Warn about inaccessible types in method signatures.
        "-Ywarn-inaccessible",
        // Warn when a type argument is inferred to be `Any`.
        "-Ywarn-infer-any",
        // Warn when non-nullary `def f()' overrides nullary `def f'
        "-Ywarn-nullary-override",
        // Warn when nullary methods return Unit.
        "-Ywarn-nullary-unit",
        // Warn when numerics are widened.
        "-Ywarn-numeric-widen"
        // Warn when imports are unused.
        //"-Ywarn-unused-import"
      )
    },
    scalafmtOnCompile in ThisBuild := true,
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots"),
      "Seasar2 Repository" at "http://maven.seasar.org/maven2",
      Resolver.bintrayRepo("danslapman", "maven")
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.0"),
    libraryDependencies ++= Seq(
      ScalaTest.core % Test
    )
  )
}
