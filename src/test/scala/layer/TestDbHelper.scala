package layer

import doobie.implicits.*
import doobie.{ConnectionIO, Transactor}
import services.DbConnector
import zio.*
import zio.interop.catz.*

case class TestDbHelper(xa: Transactor[Task]) {
  def truncateTable(): ConnectionIO[Int] = {
    sql"""TRUNCATE TABLE flags RESTART IDENTITY """.update.run
  }

  def executeTruncate(): ZIO[Any, Throwable, Unit] = {
    for {
      _ <- truncateTable().transact(xa)
    } yield ()
  }
}

object TestDbHelper {
  val layer: ZLayer[DbConnector, Nothing, TestDbHelper] = ZLayer {
    for {
      dbConnector <- ZIO.service[DbConnector]
    } yield TestDbHelper(dbConnector.transactor)
  }
    
//  def cleanDatabase(): Task[Unit] = {
//    for {
//      _ <- truncateTable().transact(xa)
//    } yield ()
//  }
  
  def truncateTable(): ConnectionIO[Int] = {
    sql"""TRUNCATE TABLE flags RESTART IDENTITY """.update.run
  }

}
