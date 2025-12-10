ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "toggler"
  )

val zioVersion = "2.1.22"

libraryDependencies ++= {
  val zioVersion = "2.1.22"
  Seq(
    "dev.zio" %% "zio" % zioVersion,
    "dev.zio" %% "zio-streams" % zioVersion,
    "dev.zio" %% "zio-test" % zioVersion,
    "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    "dev.zio" %% "zio-http" % "3.7.1",
    "dev.zio" %% "zio-json" % "0.7.45",
    "dev.zio" %% "zio-schema-json" % "0.7.45"
  )
}

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")