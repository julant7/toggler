package service

import cats.implicits.*
import config.DbConfig
import doobie.*
import zio.*
import zio.interop.catz.*

case class DbConnector(val transactor: Transactor[Task]) {

}

object DbConnector {
  val layer: ZLayer[Any, Throwable, DbConnector] = ZLayer{
    (for {
      config <- DbConfig.refFromFile()
    } yield DbConnector(Transactor.fromDriverManager[Task](
      driver = config.driverClassName,
      url = config.jdbcUrl,
      user = config.user,
      password = config.password,
      logHandler = None
    ))).mapError(err => throw new RuntimeException("ewwe"))

  }

//  def xa[T](implicit async: Async[Task]): ZIO[T, Throwable, Transactor[Task]] = (for {
//    config <- DbConfig.refFromFile()
//  } yield Transactor.fromDriverManager[Task](
//    driver = config.driverClassName,
//    url = config.jdbcUrl,
//    user = config.user,
//    password = config.password,
//    logHandler = None
//  )).mapError(err => throw new RuntimeException("ewwe"))

}
