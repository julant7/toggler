package service

import cats.effect.Async
import config.DbConfig
import doobie.Transactor
import zio.*
import cats.implicits.*

case class DbConnector(val config: DbConfig) {

}

object DbConnector {
  val layer: ZLayer[Any, Throwable, DbConnector] = ZLayer{
    for {
      config <- DbConfig.refFromFile()
    } yield DbConnector(config)
  }

  def xa[T](implicit async: Async[Task]): ZIO[T, Throwable, Transactor[Task]] = (for {
    config <- DbConfig.refFromFile()
  } yield Transactor.fromDriverManager[Task](
    driver = config.driverClassName,
    url = config.jdbcUrl,
    user = config.user,
    password = config.password,
    logHandler = None
  )).mapError(err => throw new RuntimeException("ewwe"))

}
