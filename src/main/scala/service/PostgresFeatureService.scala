package service

import doobie.*
import doobie.implicits.*
import doobie.postgres.circe.json.implicits.{pgDecoderGet, pgEncoderPut}
import dto.*
import entity.{FeatureFlag, Rule, RuleEvaluator}
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import zio.*
import zio.interop.catz.*

import java.sql.Timestamp
import java.time.LocalDateTime

class PostgresFeatureService(flags: Ref[Map[String, FeatureFlag]], snapshotDate: Ref[Timestamp], xa: Transactor[Task]) extends FeatureService {

  implicit val metaListRule: Meta[List[Rule]] = new Meta(pgDecoderGet, pgEncoderPut)
  implicit val metaJson: Meta[Json] = new Meta(pgDecoderGet, pgEncoderPut)

  override def isEnabled(featureKey: String, request: CheckRequest): UIO[CheckResponse] = {
    for {
      flags <- flags.get()
    } yield {
      CheckResponse(flags.get(featureKey).exists(feat => feat.rules.exists(rule => RuleEvaluator.evaluate(rule, request, featureKey))))
    }
  }

  override def getAll: UIO[GetFlagsResponse] = flags.get.map(flags => GetFlagsResponse(toDTO(flags.values.toList)))

  override def upsert(request: AddFlagRequest): ZIO[Any, Throwable, Unit] = {
    for {
      updatedFlag <- insert(request).transact(xa)
      _ <- flags.update(_ + (updatedFlag.key -> updatedFlag))
    } yield ()
  }

  override def updateCache(): zio.ZIO[Any, Throwable, Unit] = {
    for {
      curFlags <- flags.get()
      date <- snapshotDate.get()
      updatedFlags <- updateCache(curFlags, date).transact(xa)
      _ <- flags.update(map => {
        val keysDeleted = updatedFlags.filter(_.is_deleted).map(_.key)
        val flagsNotDeleted = updatedFlags.filterNot(_.is_deleted).map(el => (el.key -> el))
        (map -- keysDeleted) ++ flagsNotDeleted
      })
      _ <- snapshotDate.update(_ => Timestamp.valueOf(LocalDateTime.now()))

    } yield ()
  }

  override def delete(request: DeleteFlagRequest): ZIO[Any, Throwable, Unit] = {
    for {
      _ <- markRemoved(request, Timestamp.valueOf(LocalDateTime.now())).transact(xa)
      _ <- flags.update(_.removed(request.key))
    } yield ()
  }

  def allFlags(): ZIO[Any, Throwable, List[FeatureFlag]] = PostgresFeatureService.execute(xa)

  private def dropTable: doobie.ConnectionIO[Int] =
    sql"""DROP TABLE IF EXISTS FLAGS""".update.run

  private def markRemoved(request: DeleteFlagRequest, updatedAt: Timestamp): doobie.ConnectionIO[Int] = {
    sql"""UPDATE FLAGS
           SET is_deleted = true, updated_at = $updatedAt
           WHERE key = ${request.key}""".stripMargin.update.run
  }


  private val app: ConnectionIO[Unit] = for {
    _ <- dropTable
    _ <- PostgresFeatureService.createTable
  } yield ()

  private def toDTO(flags: List[FeatureFlag]): List[GetFlagResponse] = {
    flags.map(flag => GetFlagResponse(
      flagId = flag.flag_id,
      key = flag.key,
      rules = flag.rules,
      created_at = flag.created_at.toString,
      updated_at = flag.updated_at.toString
    ))
  }


  private def updateCache(flags: Map[String, FeatureFlag], snapshotDate: Timestamp): ConnectionIO[List[FeatureFlag]] = {
    sql"""SELECT *
             FROM flags
             WHERE updated_at > $snapshotDate""".query[FeatureFlag].to[List]
  }



  private def insert(request: AddFlagRequest): doobie.ConnectionIO[FeatureFlag] = {
    sql"""INSERT INTO FLAGS(key, rules)
                   VALUES(${request.key}, ${request.rules.asJson})
                   ON CONFLICT(key)
                   DO UPDATE SET rules = EXCLUDED.rules
                   RETURNING flag_id, key, rules, created_at, updated_at
                 """.query[FeatureFlag].unique
  }


}
object PostgresFeatureService {

  implicit val metaListRule: Meta[List[Rule]] = new Meta(pgDecoderGet, pgEncoderPut)
  implicit val metaJson: Meta[Json] = new Meta(pgDecoderGet, pgEncoderPut)

  val layer: ZLayer[DbConnector, Throwable, FeatureService] = ZLayer{

    val flagMap: ZIO[DbConnector, Throwable, Map[String, FeatureFlag]] = {
      for {
        dbConnector <- ZIO.service[DbConnector]
        flags <- execute(dbConnector.transactor)
      } yield {
        flags.map(el => (el.key -> el)).toMap
      }
    }

    val timestampNow = Timestamp.valueOf(LocalDateTime.now())
    val postgresFeatureService =
      for {
        flags <- flagMap
        refFlags <- Ref.make(flags)
        refDate <- Ref.make(timestampNow)
        dbConnector <- ZIO.service[DbConnector]
      } yield PostgresFeatureService(refFlags, refDate, dbConnector.transactor)

    for {
      service <- postgresFeatureService
      updates <- service.updateCache().repeat(Schedule.fixed(10.seconds)).fork
    } yield service
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

  private def loadFlags: ConnectionIO[List[FeatureFlag]] = {
    sql"""SELECT * FROM FLAGS""".query[FeatureFlag].to[List]
  }

  private def execute(xa: Transactor[Task]): ZIO[Any, Throwable, List[FeatureFlag]] = {
    for {
      _ <- createTable.transact(xa)
      //      _ <- insert(AddFlagRequest(key = "always_on8", rules = List(Rule(AlwaysOn())))).transact(xa)
      hh <- loadFlags.transact(xa)
    } yield hh
  }

}