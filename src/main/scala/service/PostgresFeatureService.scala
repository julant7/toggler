package service

import doobie.*
import doobie.implicits.*
import doobie.postgres.circe.json.implicits.{pgDecoderGet, pgEncoderPut}
import dto.{AddFlagRequest, CheckRequest, CheckResponse, GetFlagsResponse}
import entity.{FeatureFlag, Rule, RuleEvaluator}
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import zio.*
import zio.interop.catz.*


class PostgresFeatureService(flags: Ref[Map[String, FeatureFlag]]) extends FeatureService {

  override def isEnabled(featureKey: String, request: CheckRequest): UIO[CheckResponse] = {
    for {
      flags <- flags.get()
    } yield {
      CheckResponse(flags.get(featureKey).exists(feat => feat.rules.exists(rule => RuleEvaluator.evaluate(rule, request, featureKey))))
    }
  }

  override def getAll: UIO[GetFlagsResponse] = flags.get.map(flags => GetFlagsResponse(flags.values.toList))

  override def upsert(request: AddFlagRequest): ZIO[Any, Throwable, Unit] = {
    for {
      xa <- DbConnector.xa
      updatedFlag: FeatureFlag <- PostgresFeatureService.insert(request).transact(xa)
      _ <- flags.update(_ + (updatedFlag.key -> updatedFlag))
    } yield ()
  }

}
object PostgresFeatureService {

  implicit val metaListRule: Meta[List[Rule]] = new Meta(pgDecoderGet, pgEncoderPut)
  implicit val metaJson: Meta[Json] = new Meta(pgDecoderGet, pgEncoderPut)

  val layer: ZLayer[Any, Throwable, FeatureService] = ZLayer{

    val flagMap: ZIO[Any, Throwable, Map[String, FeatureFlag]] = {
      for {
        flags <- allFlags()
      } yield {
        flags.map(el => (el.key -> el)).toMap
      }
    }
    for {
      flags <- flagMap
      ref <- Ref.make(flags)
    } yield PostgresFeatureService(ref)
  }

  implicit def unsafe: Unsafe = null.asInstanceOf[zio.Unsafe]

  def createTable: doobie.ConnectionIO[Int] =
    sql"""|CREATE TABLE IF NOT EXISTS FLAGS(
          |id SERIAL PRIMARY KEY,
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

  def loadUsers: ConnectionIO[List[FeatureFlag]] = {
    sql"""SELECT * FROM FLAGS""".query[FeatureFlag].to[List]
  }

  def execute(): ZIO[Any, Throwable, List[FeatureFlag]] = {
    for {
      xa <- DbConnector.xa
      _ <- dropTable.transact(xa)
      _ <- createTable.transact(xa)
//      _ <- insert(AddFlagRequest(key = "always_on8", rules = List(Rule(AlwaysOn())))).transact(xa)
      hh <- loadUsers.transact(xa)
    } yield hh
  }



  val app: ConnectionIO[Unit] = for {
    _ <- dropTable
    _ <- createTable
  } yield ()

  def allFlags(): ZIO[Any, Throwable, List[FeatureFlag]] = execute()

}