import org.scalafmt.sbt.ScalafmtPlugin.autoImport._
import sbt.Keys._
import sbt._

object Settings {

  val coreSettings = Seq(
    organization := "com.github.yoshiyoshiifujii",
    scalaVersion := "2.12.8",
    scalacOptions ++= {
      Seq(
        "-feature",
        "-deprecation",
        "-unchecked",
        "-encoding",
        "UTF-8",
        "-language:_",
        "-Ypartial-unification",
        "-Ydelambdafy:method",
        "-target:jvm-1.8"
      )
    },
    scalafmtOnCompile in ThisBuild := true,
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots"),
      "Seasar2 Repository" at "http://maven.seasar.org/maven2",
      Resolver.bintrayRepo("danslapman", "maven")
    ),
    libraryDependencies ++= Seq(
      ScalaTest.core % Test
    )
  )
}
