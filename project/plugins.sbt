resolvers ++= Seq(
  "Seasar Repository" at "http://maven.seasar.org/maven2/",
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

addSbtPlugin("org.scalastyle"         %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("org.scalameta"          % "sbt-scalafmt"           % "2.0.0")
addSbtPlugin("net.virtual-void"       % "sbt-dependency-graph"   % "0.9.2")
addSbtPlugin("io.github.davidmweber"  % "flyway-sbt"             % "5.2.0")
addSbtPlugin("jp.co.septeni-original" % "sbt-dao-generator"      % "1.0.8")
addSbtPlugin("com.chatwork"           % "sbt-wix-embedded-mysql" % "1.0.9")
