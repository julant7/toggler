import zio.{Ref, UIO, ZIO, ZLayer}

class InMemoryFeatureService(featureFlags: Ref[Map[String, FeatureFlag]]) extends FeatureService {
  override def isEnabled(featureKey: String, context: UserContext): UIO[Boolean] = {
    featureFlags
      .get()
      .map(_.get(featureKey))
      .map(feat => feat
        .exists(_.rules
          .exists(RuleEvaluator.evaluate(_, context, featureKey))
        )
      )
  }
}

object InMemoryFeatureService {
  val layer: ZLayer[Any, Nothing, FeatureService] =
    ZLayer {
      for {
        map <- ZIO.succeed(Map("priv" -> FeatureFlag("priv", List(Rule(AlwaysOn()))),
                               "cool" -> FeatureFlag("cool", List(Rule(UserList(Set("user1", "user7"))))),
                               "country" -> FeatureFlag("country", List(Rule(CountryAllow(Set("Russia", "India")))))))
        featureFlags <- Ref.make(map)
      } yield InMemoryFeatureService(featureFlags)
    }
}
