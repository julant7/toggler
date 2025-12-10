package service

import dto.{AddFlagRequest, CheckRequest, CheckResponse}
import entity.{AlwaysOn, CountryAllow, FeatureFlag, Rule, RuleEvaluator, UserList}
import zio.*
import zio.http.*

import scala.runtime.Nothing$

class InMemoryFeatureService(featureFlags: Ref[Map[String, FeatureFlag]]) extends FeatureService {
  override def isEnabled(featureKey: String, request: CheckRequest): ZIO[Any, Response, CheckResponse] = {
    
    (for {
      flags <- featureFlags.get()
    } yield {
      request match {
        case x if x == null => null
        case _ => CheckResponse(flags.get(featureKey).exists(feat => feat.rules.exists(rule => RuleEvaluator.evaluate(rule, request, featureKey))))
      }
    }).filterOrElse(_ != null)(ZIO.fail(Response.error(Status.BadRequest)))


//    (for {
//      flags <- featureFlags.get()
//    } yield {
//      CheckResponse(flags.get(featureKey).exists(feat => feat.rules.exists(rule => RuleEvaluator.evaluate(rule, request, featureKey))))
//    }).orElseFail()


  }

  override def isEnabled1(featureKey: String, request: Task[CheckRequest]): ZIO[Any, Response, CheckResponse] = {
    val zioThrowable =
      (for {
        flags <- featureFlags.get()
        req <- request
      } yield {
        CheckResponse(flags.get(featureKey).exists(feat => feat.rules.exists(rule => RuleEvaluator.evaluate(rule, req, featureKey))))
      })
    zioThrowable.orElseFail(Response.error(Status.BadRequest))
  }
//    val ff = for {
//      featureFlags <- featureFlags.get()
//      curFeatureKey <- featureFlags.get(featureKey)
//      rule <- curFeatureKey.rules
//      checkRequest <- request
//    } yield RuleEvaluator.evaluate(rule = rule, request = checkRequest, key = curFeatureKey.key)
//    
//    val ff3 = for {
//      featureFlags <- featureFlags.get()
//      curFeatureKey <- featureFlags.get(featureKey)
//      rule <- curFeatureKey.rules
//      checkRequest <- request
//    } yield RuleEvaluator.evaluate(rule = rule, request = checkRequest, key = curFeatureKey.key)

//    val f2f = for {
//      featureFlags <- featureFlags.get()
//      curFeatureKey <- featureFlags.get(featureKey)
//    } yield curFeatureKey.rules
//
//    val yy = featureFlags
//      .get()
//      .map(_.get(featureKey))
//      .map(feat => feat
//        .map(flag => flag.rules
//          .map(rule => {
//            request.map(RuleEvaluator.evaluate(rule, _, featureKey))
//          })
//        )
//      )
//
//    val yy2 = featureFlags
//      .get()
//      .map(_.get(featureKey))
//      .map(feat => feat
//        .map(flag => flag.rules
//          .map(rule => {
//            request.map(RuleEvaluator.evaluate(rule, _, featureKey))
//          })
//        )
//      )
//
//    val ee = featureFlags
//      .get()
//      .map(_.get(featureKey))
//      .map(feat => feat.map(flag => flag.rules.map(rule => RuleEvaluator.evaluate(rule, request, featureKey))))
//      .map(_.foldLeft[](ZIO.succeed(true)))
//
//
//
//    val x = Some(5)
//    val y = x.map(_ => 9)
//    featureFlags
//      .get()
//      .map(_.get(featureKey))
//      .map(feat => feat
//        .exists(flag => flag.rules
//          .exists(rule => {
//            RuleEvaluator.evaluate(rule, request, featureKey)
//          })
//        )
//      )
//  }

  override def upsert(featureFlag: ZIO[Any, Response, AddFlagRequest]): UIO[Unit] = {
    for {
      flag <- featureFlag
    } yield featureFlags.update(_ + (flag.key -> FeatureFlag(flag.key, flag.rules)))
    ZIO.succeed(4)
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
