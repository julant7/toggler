import cats.implicits.*
import doobie.Transactor
import services.Connector
import zio.{Task, ZLayer}
import zio.interop.catz.*

class TestDbConnector(transactor: Transactor[Task]) extends Connector(transactor) {

}

object TestDbConnector {
  val layer: ZLayer[Any, Throwable, TestDbConnector] = ZLayer{
    (for {
      config <- TestDbConfig.refFromFile()
    } yield TestDbConnector(Transactor.fromDriverManager[Task](
      driver = config.driverClassName,
      url = config.jdbcUrl,
      user = config.user,
      password = config.password,
      logHandler = None
    ))).mapError(err => throw new RuntimeException("Error while connecting to database"))
  }
}