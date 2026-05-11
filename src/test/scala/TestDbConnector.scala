import cats.implicits.*
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.Transactor
import entity.FeatureFlag
import services.{Connector, DbConnector}
import zio.{Task, ZIO, ZLayer}
import zio.interop.catz.*
import doobie.*
import doobie.implicits.*

class TestDbConnector(transactor: Transactor[Task]) extends Connector(transactor)

object TestDbConnector {
  val layer: ZLayer[PostgreSQLContainer, Nothing, DbConnector] = ZLayer{
    for {
      container <- ZIO.service[PostgreSQLContainer]
    } yield DbConnector(Transactor.fromDriverManager[Task](
      driver = container.driverClassName,
      url = container.jdbcUrl,
      user = container.username,
      password = container.password,
      logHandler = None
    ))
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