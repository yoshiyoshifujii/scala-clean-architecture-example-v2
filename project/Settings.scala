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
        "-Ywarn-numeric-widen",
        // Warn when imports are unused.
        "-Ywarn-unused-import"
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
    ),
    dependencyOverrides ++= Seq(
      "org.slf4j"                  % "slf4j-api"               % "1.7.26",
      "org.scala-lang.modules"     %% "scala-xml"              % "1.2.0",
      "com.typesafe.akka"          %% "akka-slf4j"             % "2.5.22",
      "com.typesafe.akka"          %% "akka-actor"             % "2.5.22",
      "com.typesafe.akka"          %% "akka-stream"            % "2.5.22",
      "com.typesafe.akka"          %% "akka-cluster"           % "2.5.22",
      "com.typesafe.akka"          %% "akka-cluster-sharding"  % "2.5.22",
      "com.typesafe.akka"          %% "akka-discovery"         % "2.5.22",
      "com.typesafe.akka"          %% "akka-persistence"       % "2.5.22",
      "com.typesafe.akka"          %% "akka-persistence-query" % "2.5.22",
      "com.typesafe.akka"          %% "akka-testkit"           % "2.5.22",
      "com.typesafe.akka"          %% "akka-http"              % "10.1.8",
      "com.typesafe.akka"          %% "akka-http-core"         % "10.1.8",
      "com.typesafe.akka"          %% "akka-parsing"           % "10.1.8",
      "org.typelevel"              %% "cats-core"              % "1.5.0",
      "org.typelevel"              %% "cats-kernel"            % "1.5.0",
      "org.typelevel"              %% "cats-macros"            % "1.5.0",
      "org.typelevel"              %% "machinist"              % "0.6.6",
      "com.typesafe"               % "config"                  % "1.3.1",
      "com.typesafe"               %% "ssl-config-core"        % "0.3.6",
      "io.kamon"                   %% "kamon-core"             % "1.1.6",
      "io.netty"                   % "netty-codec-http"        % "4.1.33.Final",
      "io.netty"                   % "netty-handler"           % "4.1.33.Final",
      "org.scala-lang.modules"     %% "scala-java8-compat"     % "0.9.0",
      "com.google.guava"           % "guava"                   % "27.1-jre",
      "com.lihaoyi"                %% "sourcecode"             % "0.1.4",
      "org.reactivestreams"        % "reactive-streams"        % "1.0.2",
      "com.google.errorprone"      % "error_prone_annotations" % "2.3.2",
      "de.heikoseeberger"          %% "akka-http-circe"        % "1.25.2",
      "io.circe"                   %% "circe-parser"           % "0.11.1",
      "io.circe"                   %% "circe-generic"          % "0.11.1",
      "io.circe"                   %% "circe-core"             % "0.11.1",
      "io.swagger.core.v3"         % "swagger-jaxrs2"          % "2.0.8",
      "io.swagger.core.v3"         % "swagger-core"            % "2.0.8",
      "io.swagger.core.v3"         % "swagger-models"          % "2.0.8",
      "io.swagger.core.v3"         % "swagger-annotations"     % "2.0.8",
      "com.fasterxml.jackson.core" % "jackson-annotations"     % "2.9.8",
      "io.kamon"                   %% "kamon-akka-http-2.5"    % "1.1.2"
    ),
  )
}
