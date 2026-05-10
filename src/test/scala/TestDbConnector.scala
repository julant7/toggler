import cats.implicits.*
import doobie.Transactor
import entity.FeatureFlag
import services.Connector
import services.FeatureServiceImpl.createTable
import zio.{Task, ZIO, ZLayer}
import zio.interop.catz.*
import doobie.*
import doobie.implicits.*

class TestDbConnector(transactor: Transactor[Task]) extends Connector(transactor)

object TestDbConnector {
  val layer: ZLayer[Nothing, Any, Option[Any]] = ZLayer{
    (for {
      config <- TestDbConfig.refFromFile()
    } yield TestDbConnector(Transactor.fromDriverManager[Task](
      driver = config.driverClassName,
      url = config.jdbcUrl,
      user = config.user,
      password = config.password,
      logHandler = None
    ))).forEachZIO(conn => createTable(conn.transactor))
  }

  def createTable: doobie.ConnectionIO[Int] =
    sql"""|CREATE TABLE IF NOT EXISTS FLAGS(
          |flag_id SERIAL PRIMARY KEY NOT NULL,
          |key VARCHAR NOT NULL UNIQUE NOT NULL,
          |rules JSON NOT NULL,
          |created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
          |updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
          |is_deleted BOOLEAN DEFAULT FALSE NOT NULL
          |)""".stripMargin.update.run


  def createTable(xa: Transactor[Task]): zio. ZIO[Nothing, Any, Any] = {
    createTable.transact(xa)
  }
}