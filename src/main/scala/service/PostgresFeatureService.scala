package service

import doobie.*
import doobie.implicits.*
import fs2.Stream
import zio.{FiberRefs, Ref, RuntimeFlags, Task, UIO, Unsafe, ZEnvironment, ZIO, ZLayer}
import zio.interop.catz.*
import dto.{AddFlagRequest, CheckRequest, CheckResponse}
import entity.FeatureFlag
import service.PostgresFeatureService.insert

case class Flags(id: String, key: String, rules: String)

class PostgresFeatureService(flags: Ref[List[Flags]]) extends FeatureService {

  override def isEnabled(featureKey: String, request: CheckRequest): UIO[CheckResponse] = ???

//  override def upsert(newFeatureFlag: AddFlagRequest): UIO[Unit] = {
//    for {
//      res <- ZIO.succeed(Unit)
//      _ <- fs2.Stream.eval(insert("always_on", """{"always_on": ""})"""))
//    } yield res
//  }

  override def getAll: UIO[Map[String, FeatureFlag]] = ???
  
  def getAllFlags: UIO[List[Flags]] = flags.get

  override def upsert(newFeatureFlag: AddFlagRequest): UIO[Unit] = ???
}
object PostgresFeatureService {
  val layer: ZLayer[Any, Nothing, FeatureService] = ZLayer{
    for {
      listFlags <- ZIO.succeed(allFlags)
      refFlags <- Ref.make(listFlags)
    } yield PostgresFeatureService(refFlags)
  }

  implicit val zioRuntime: zio.Runtime[Any] = zio.Runtime(ZEnvironment.empty, FiberRefs.empty, RuntimeFlags.default)

  implicit def unsafe: Unsafe = null.asInstanceOf[zio.Unsafe]

  def xa: Transactor[Task] =
  Transactor.fromDriverManager[Task](
    driver = "org.h2.Driver",
    url = "jdbc:h2:mem:users;DB_CLOW_DELAY=-1",
    user = "",
    password = "",
    logHandler = None
  )

  def createTable: doobie.ConnectionIO[Int] =
    //  sql"""|CREATE TABLE IF NOT EXISTS USERS(
    //        |id INT SERIAL UNIQUE,
    //        |name VARCHAR NOT NULL UNIQUE,
    //        |age SMALLINT
    //        |)""".stripMargin.update.run

    sql"""|CREATE TABLE IF NOT EXISTS FLAGS(
          |id INT SERIAL UNIQUE,
          |key VARCHAR NOT NULL UNIQUE,
          |rules JSONB
          |)""".stripMargin.update.run

  def dropTable: doobie.ConnectionIO[Int] =
    sql"""DROP TABLE IF EXISTS FLAGS""".update.run

  def insert(key: String, rules: String): doobie.ConnectionIO[Int] =
    sql"INSERT INTO FLAGS(key, rules) values($key, $rules)".update.run

  def loadUsers: Stream[doobie.ConnectionIO, Flags] =
    sql"""SELECT * FROM FLAGS""".query[Flags].stream

  val doobieApp: Stream[doobie.ConnectionIO, Flags] = for {
    _ <- fs2.Stream.eval(dropTable)
    _ <- fs2.Stream.eval(createTable)
    _ <- fs2.Stream.eval(insert("always_on", """{"always_on": ""})"""))
    _ <- fs2.Stream.eval(insert("country", """{"countries": ["Russia", "India"]})"""))
    u <- loadUsers
  } yield u

  val run: Stream[Task, Flags] = doobieApp.transact(xa)

  val allFlags: List[Flags] =
    Unsafe.unsafe(implicit u => zioRuntime.unsafe.run(run.compile.toList)).getOrThrowFiberFailure()

}