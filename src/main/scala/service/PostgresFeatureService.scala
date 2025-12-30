package service

import doobie.*
import doobie.implicits.*
import doobie.postgres.circe.json.implicits.{pgDecoderGet, pgEncoderPut}
import dto.{AddFlagRequest, CheckRequest, CheckResponse, GetFlagResponse, GetFlagsResponse}
import entity.{FeatureFlag, Rule, RuleEvaluator}
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import service.PostgresFeatureService.toDTO
import zio.*
import zio.interop.catz.*

import java.sql.Timestamp
import java.time.LocalDateTime


class PostgresFeatureService(flags: Ref[Map[String, FeatureFlag]], xa: Transactor[Task]) extends FeatureService {

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
      updatedFlag: FeatureFlag <- PostgresFeatureService.insert(request).transact(xa)
      _ <- flags.update(_ + (updatedFlag.key -> updatedFlag))
    } yield ()
  }

  override def updateCache(): zio.ZIO[Any, Throwable, Unit] = {
    for {
      curFlags <- flags.get()
      updatedFlags <- PostgresFeatureService.updateCache(curFlags, Timestamp.valueOf(LocalDateTime.now())).transact(xa)
    } yield updatedFlags.foreach(flag => flags.update(_ + (flag.key -> flag)))
  }


  def allFlags(): ZIO[Any, Throwable, List[FeatureFlag]] = PostgresFeatureService.execute(xa)

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
    for {
      flags <- flagMap
      ref <- Ref.make(flags)
      dbConnector <- ZIO.service[DbConnector]
    } yield PostgresFeatureService(ref, dbConnector.transactor)
  }

  implicit def unsafe: Unsafe = null.asInstanceOf[zio.Unsafe]

  def createTable: doobie.ConnectionIO[Int] =
    sql"""|CREATE TABLE IF NOT EXISTS FLAGS(
          |flag_id SERIAL PRIMARY KEY,
          |key VARCHAR NOT NULL UNIQUE,
          |rules JSON
          |)""".stripMargin.update.run

  def dropTable: doobie.ConnectionIO[Int] =
    sql"""DROP TABLE IF EXISTS FLAGS""".update.run

  def insert(request: AddFlagRequest): doobie.ConnectionIO[FeatureFlag] = {
    sql"""INSERT INTO FLAGS(key, rules)
         VALUES(${request.key}, ${request.rules.asJson})
         ON CONFLICT(key)
         DO UPDATE SET rules = EXCLUDED.rules
         RETURNING id, key, rules
       """.query[FeatureFlag].unique
  }

  def updateCache(flags: Map[String, FeatureFlag], date: Timestamp): ConnectionIO[List[FeatureFlag]] = {
    sql"""SELECT *
         FROM flags
         WHERE id NOT IN ${flags.keySet.mkString(",")} OR date_updated > $date""".query[FeatureFlag].to[List]
  }

  def loadUsers: ConnectionIO[List[FeatureFlag]] = {
    sql"""SELECT * FROM FLAGS""".query[FeatureFlag].to[List]
  }
  
  val app: ConnectionIO[Unit] = for {
    _ <- dropTable
    _ <- createTable
  } yield ()

  private def execute(xa: Transactor[Task]): ZIO[Any, Throwable, List[FeatureFlag]] = {
    for {
//      _ <- dropTable.transact(xa)
//      _ <- createTable.transact(xa)
      //      _ <- insert(AddFlagRequest(key = "always_on8", rules = List(Rule(AlwaysOn())))).transact(xa)
      hh <- loadUsers.transact(xa)
    } yield hh
  }

  def toDTO(flags: List[FeatureFlag]): List[GetFlagResponse] = {
    flags.map(flag => GetFlagResponse(
      id = flag.id,
      key = flag.key,
      rules = flag.rules,
      created_at = flag.created_at.toString,
      updated_at = flag.updated_at.toString
    ))

  }

}