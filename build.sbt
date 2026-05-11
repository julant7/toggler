ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "toggler"
  )

val zioVersion = "2.1.23"
val zioTestkitVersion = "3.8.1"
val flywayVersion = "12.6.0"
val logbackVersion = "1.5.32"
val catsEffectVersion = "3.6.3"
val doobieCoreVersion = "1.0.0-RC11"
val zioHttpVersion = "3.7.4"
val zioJsonVersion = "0.7.45"
val zioInteropCatsVersion = "23.1.0.12"
val zioConfigVersion = "4.0.6"
val circeVersion = "0.14.15"
val testcontainersVersion = "0.44.1"
val orgPostgresql = "42.7.7"

scalacOptions ++= Seq("-Xmax-inlines", "50")
libraryDependencies ++= {

  Seq(
    "dev.zio"        %% "zio"                             % zioVersion,
    "dev.zio"        %% "zio-streams"                     % zioVersion,
    "dev.zio"        %% "zio-test"                        % zioVersion             % Test,
    "dev.zio"        %% "zio-test-sbt"                    % zioVersion             % Test,
    "dev.zio"        %% "zio-http-testkit"                % zioTestkitVersion      % Test,
    "dev.zio"        %% "zio-http"                        % zioHttpVersion,
    "dev.zio"        %% "zio-json"                        % zioJsonVersion,
    "dev.zio"        %% "zio-interop-cats"                % zioInteropCatsVersion,
    "dev.zio"        %% "zio-config"                      % zioConfigVersion,
    "dev.zio"        %% "zio-config-magnolia"             % zioConfigVersion,
    "dev.zio"        %% "zio-config-typesafe"             % zioConfigVersion,
    "org.flywaydb"   % "flyway-core"                      % flywayVersion,
    "ch.qos.logback" % "logback-classic"                  % logbackVersion,
    "org.typelevel"  %% "cats-effect"                     % catsEffectVersion,
    "org.tpolecat"   %% "doobie-core"                     % doobieCoreVersion,
    "org.tpolecat"   %% "doobie-postgres"                 % doobieCoreVersion,
    "org.tpolecat"   %% "doobie-hikari"                   % doobieCoreVersion,
    "org.tpolecat"   %% "doobie-postgres-circe"           % doobieCoreVersion,
    "io.circe"       %% "circe-core"                      % circeVersion,
    "io.circe"       %% "circe-generic"                   % circeVersion,
    "io.circe"       %% "circe-parser"                    % circeVersion,
    "com.dimafeng"   %% "testcontainers-scala-postgresql" % testcontainersVersion,
  )
}

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

Test / fork := true


enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerUsername := Some("julant7")
dockerRepository := Some("docker.io")
//packageName in Docker := "julant7/toggler"

