ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "toggler"
  )

val zioVersion = "2.1.23"
val catsEffectVersion = "3.6.3"
val doobieCoreVersion = "1.0.0-RC11"
val zioHttpVersion = "3.7.1"
val zioJsonVersion = "0.7.45"
val zioInteropCatsVersion = "23.1.0.11"
val fs2Version = "3.12.2"

libraryDependencies ++= {

  Seq(
    "dev.zio" %% "zio" % zioVersion,
    "dev.zio" %% "zio-streams" % zioVersion,
    "dev.zio" %% "zio-test" % zioVersion,
    "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    "dev.zio" %% "zio-http" % zioHttpVersion,
    "dev.zio" %% "zio-json" % zioJsonVersion,
    "dev.zio" %% "zio-interop-cats" % zioInteropCatsVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "org.tpolecat" %% "doobie-core" % doobieCoreVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieCoreVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieCoreVersion,
    "co.fs2" %% "fs2-core" % fs2Version
  )
}

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")