import Settings._
import org.seasar.util.lang.StringUtil

import scala.concurrent.duration._

val baseName = "scala-clean-architecture-example-v2"

lazy val `infrastructure` = (project in file("modules/infrastructure"))
  .settings(
    name := s"$baseName-infrastructure",
    libraryDependencies ++= Seq(
        Passay.passay,
        Commons.codec,
        Huxhorn.ulid,
        Timepit.refined
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
        Beachape.enumeratum
      )
  )
  .settings(coreSettings)
  .dependsOn(infrastructure)

lazy val `usecases` = (project in file("modules/usecases"))
  .settings(
    name := s"$baseName-usecases",
    libraryDependencies ++= Seq(
        Scalaz.zio     % Test,
        Wvlet.airframe % Test
      )
  )
  .settings(coreSettings)
  .dependsOn(`domain`)

lazy val `flyway` = (project in file("tools/flyway"))
  .enablePlugins(FlywayPlugin)
  .settings(coreSettings)
  .settings(
    name := s"$baseName-flyway",
    libraryDependencies ++= Seq(MySQL.connector),
    parallelExecution in Test := false,
    wixMySQLVersion := com.wix.mysql.distribution.Version.v5_6_21,
    wixMySQLUserName := Some(dbUser),
    wixMySQLPassword := Some(dbPassword),
    wixMySQLSchemaName := dbName,
    wixMySQLPort := Some(dbPort),
    wixMySQLDownloadPath := Some(sys.env("HOME") + "/.wixMySQL/downloads"),
    //wixMySQLTempPath := Some(sys.env("HOME") + "/.wixMySQL/work"),
    wixMySQLTimeout := Some(2 minutes),
    flywayDriver := dbDriver,
    flywayUrl := dbUrl,
    flywayUser := dbUser,
    flywayPassword := dbPassword,
    flywaySchemas := Seq(dbName),
    flywayLocations := Seq(
        s"filesystem:${baseDirectory.value}/src/test/resources/db-migration/",
        s"filesystem:${baseDirectory.value}/src/test/resources/db-migration/test"
      ),
    flywayPlaceholderReplacement := true,
    flywayPlaceholders := Map(
        "engineName"                 -> "MEMORY",
        "idSequenceNumberEngineName" -> "MyISAM"
      ),
    flywayMigrate := (flywayMigrate dependsOn wixMySQLStart).value
  )

lazy val localMySQL = (project in file("tools/local-mysql"))
  .enablePlugins(FlywayPlugin)
  .settings(coreSettings)
  .settings(
    name := s"$baseName-local-mysql",
    libraryDependencies ++= Seq(MySQL.connector),
    wixMySQLVersion := com.wix.mysql.distribution.Version.v5_6_21,
    wixMySQLUserName := Some(dbUser),
    wixMySQLPassword := Some(dbPassword),
    wixMySQLSchemaName := dbName,
    wixMySQLPort := Some(3306),
    wixMySQLDownloadPath := Some(sys.env("HOME") + "/.wixMySQL/downloads"),
    wixMySQLTimeout := Some((30 seconds) * sys.env.getOrElse("SBT_TEST_TIME_FACTOR", "1").toDouble),
    flywayDriver := dbDriver,
    flywayUrl := s"jdbc:mysql://localhost:3306/$dbName?useSSL=false",
    flywayUser := dbUser,
    flywayPassword := dbPassword,
    flywaySchemas := Seq(dbName),
    flywayLocations := Seq(
        s"filesystem:${(baseDirectory in flyway).value}/src/test/resources/db-migration/",
        s"filesystem:${(baseDirectory in flyway).value}/src/test/resources/db-migration/test"
      ),
    flywayPlaceholderReplacement := true,
    flywayPlaceholders := Map(
        "engineName"                 -> "InnoDB",
        "idSequenceNumberEngineName" -> "MyISAM"
      ),
    run := (flywayMigrate dependsOn wixMySQLStart).value
  )

lazy val `interfaces` = (project in file("modules/interfaces"))
  .settings(
    name := s"$baseName-interfaces",
    libraryDependencies ++= Seq(
        Slf4j.api,
        Scalaz.zio,
        Wvlet.airframe,
        AkkaHttp.http,
        AkkaHttp.testkit % Test,
        Akka.stream,
        Akka.testkit % Test,
        Heikoseeberger.circe,
        Circe.core,
        Circe.generic,
        Circe.parser,
        MySQL.connector,
        Slick.slick,
        Slick.hikaricp,
        Debasishg.redisclient,
        T3hnar.bCrypt,
        ScalaDDDBase.memory % Test,
        Google.guava        % Test,
        ScalaTestPlus.db    % Test
      ),
    // sbt-dao-generator
    // JDBCのドライバークラス名を指定します(必須)
    driverClassName in generator := dbDriver,
    // JDBCの接続URLを指定します(必須)
    jdbcUrl in generator := dbUrl,
    // JDBCの接続ユーザ名を指定します(必須)
    jdbcUser in generator := dbUser,
    // JDBCの接続ユーザのパスワードを指定します(必須)
    jdbcPassword in generator := dbPassword,
    // カラム型名をどのクラスにマッピングするかを決める関数を記述します(必須)
    propertyTypeNameMapper in generator := {
      case "INTEGER" | "TINYINT" | "INT" | "INT UNSIGNED" => "Int"
      case "BIGINT" | "BIGINT UNSIGNED"                   => "Long"
      case "VARCHAR"                                      => "String"
      case "BOOLEAN" | "BIT"                              => "Boolean"
      case "DATE" | "TIMESTAMP" | "DATETIME"              => "java.time.Instant"
      case "DECIMAL"                                      => "BigDecimal"
      case "ENUM"                                         => "String"
    },
    propertyNameMapper in generator := {
      case "type"     => "`type`"
      case columnName => StringUtil.decapitalize(StringUtil.camelize(columnName))
    },
    tableNameFilter in generator := { tableName: String =>
      tableName.toUpperCase match {
        case "SCHEMA_VERSION"                      => false
        case "FLYWAY_SCHEMA_HISTORY"               => false
        case t if t.endsWith("ID_SEQUENCE_NUMBER") => false
        case _                                     => true
      }
    },
    outputDirectoryMapper in generator := {
      case s if s.endsWith("Spec") => (sourceDirectory in Test).value
      case s =>
        new java.io.File((scalaSource in Compile).value, "/adapters/dao/jdbc")
    },
    // モデル名に対してどのテンプレートを利用するか指定できます。
    templateNameMapper in generator := {
      case className if className.endsWith("Spec") => "template_spec.ftl"
      case _                                       => "template.ftl"
    },
    //compile in Compile := ((compile in Compile) dependsOn (generateAll in generator)).value,
    generateAll in generator := Def
        .taskDyn {
          val ga = (generateAll in generator).value
          Def
            .task {
              (wixMySQLStop in flyway).value
            }
            .map(_ => ga)
        }
        .dependsOn(flywayMigrate in flyway)
        .value,
    Test / fork := true
  )
  .settings(coreSettings)
  .dependsOn(`usecases`)

lazy val `applications-http` = (project in file("applications/http"))
  .settings(
    name := s"$baseName-applications-http",
    libraryDependencies ++= Seq(
      Logback.classic
    )
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
