package services

import cats.implicits.*
import config.DbConfig
import doobie.*
import zio.*
import zio.interop.catz.*

abstract case class Connector(transactor: Transactor[Task])

class DbConnector(transactor: Transactor[Task]) extends Connector(transactor) {

}

object DbConnector {
  val DEFAULT_DB_CONFIG_PATH = "src/main/resources/application.conf"
  def layer(fileName: String = DEFAULT_DB_CONFIG_PATH): ZLayer[Any, Throwable, DbConnector] = ZLayer{
    (for {
      config <- DbConfig.refFromFile(fileName)
    } yield DbConnector(Transactor.fromDriverManager[Task](
      driver = config.driverClassName,
      url = config.jdbcUrl,
      user = config.user,
      password = config.password,
      logHandler = None
    ))).mapError(err => throw new RuntimeException("Error while connecting to database"))

  }
}
