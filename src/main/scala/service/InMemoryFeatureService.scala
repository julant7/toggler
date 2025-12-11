package service

import dto.{AddFlagRequest, CheckRequest, CheckResponse}
import entity.{AlwaysOn, CountryAllow, FeatureFlag, Rule, RuleEvaluator, UserList}
import zio.*
import zio.json.*
import zio.http.*


class InMemoryFeatureService(featureFlags: Ref[Map[String, FeatureFlag]]) extends FeatureService {
  override def isEnabled(featureKey: String, request: CheckRequest): UIO[CheckResponse] = {
    for {
      flags <- featureFlags.get()
    } yield {
      CheckResponse(flags.get(featureKey).exists(feat => feat.rules.exists(rule => RuleEvaluator.evaluate(rule, request, featureKey))))
    }
  }

  override def upsert(newFeatureFlag: AddFlagRequest): UIO[Unit] = {
    for {
      _ <- featureFlags.update(_ + (newFeatureFlag.key -> FeatureFlag(newFeatureFlag.key, newFeatureFlag.rules)))
      rn <- featureFlags.get
    } yield ()
  }

  override def getAll: UIO[Map[String, entity.FeatureFlag]] = {
    featureFlags.get
  }
}

object InMemoryFeatureService {
  val layer: ZLayer[Any, Nothing, FeatureService] =
    ZLayer {
      for {
        map <- ZIO.succeed(Map("entity" -> FeatureFlag("entity", List(Rule(AlwaysOn()))),
                               "cool" -> FeatureFlag("cool", List(Rule(UserList(Set("user1", "user7"))))),
                               "country" -> FeatureFlag("country", List(Rule(CountryAllow(Set("Russia", "India")))))))
        featureFlags <- Ref.make(map)
      } yield InMemoryFeatureService(featureFlags)
    }
}
