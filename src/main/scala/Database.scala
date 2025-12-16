import doobie.*
import doobie.implicits.*
import fs2.Stream
import zio.{FiberRefs, RuntimeFlags, Task, Unsafe, ZEnvironment}
import zio.interop.catz.*
import doobie.hikari.HikariTransactor

implicit val zioRuntime: zio.Runtime[Any] = zio.Runtime(ZEnvironment.empty, FiberRefs.empty, RuntimeFlags.default)

implicit def unsafe: Unsafe = null.asInstanceOf[zio.Unsafe]

case class User(id: String, name: String, age: Int)

case class Flags(id: String, key: String, rules: String)

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

def loadUsers: Stream[doobie.ConnectionIO, User] =
  sql"""SELECT * FROM FLAGS""".query[User].stream

val doobieApp: Stream[doobie.ConnectionIO, User] = for {
  _ <- fs2.Stream.eval(dropTable)
  _ <- fs2.Stream.eval(createTable)
  _ <- fs2.Stream.eval(insert("always_on", """{"always_on": ""})"""))
  _ <- fs2.Stream.eval(insert("country", """{"countries": ["Russia", "India"]})"""))
  u <- loadUsers
} yield u

val run: Stream[Task, User] = doobieApp.transact(xa)

val allUsers: List[User] =
  Unsafe.unsafe(implicit u => zioRuntime.unsafe.run(run.compile.toList)).getOrThrowFiberFailure()
