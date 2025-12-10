import Main.environment
import entity.UserContext
import service.{FeatureService, InMemoryFeatureService}
import zio.ZIO
import zio.test.*

object MainSpec extends ZIOSpecDefault {
  override def spec = suite("TestingInMemoryFeatureService")(
//    test("always on") {
//      for {
//        service <- ZIO.service[FeatureService]
//        isEnabled <- service.isEnabled("entity", UserContext("124", Map("entity" -> "entity")))
//      } yield assertTrue(isEnabled)
//    },
//    test("user in UserList") {
//      for {
//        service <- ZIO.service[FeatureService]
//        isEnabled <- service.isEnabled("cool", UserContext("user7", Map("cool" -> "user7")))
//      } yield assertTrue(isEnabled)
//    },
//    test("user not in UserList") {
//      for {
//        service <- ZIO.service[FeatureService]
//        isEnabled <- service.isEnabled("cool", UserContext("user8", Map("cool" -> "user8")))
//      } yield assertTrue(!isEnabled)
//    },
//    test("user's country in CountryAllow") {
//      for {
//        service <- ZIO.service[FeatureService]
//        isEnabled <- service.isEnabled("country", UserContext("user8", Map("country" -> "India")))
//      } yield assertTrue(isEnabled)
//    },
//    test("user's country not in CountryAllow") {
//      for {
//        service <- ZIO.service[FeatureService]
//        isEnabled <- service.isEnabled("country", UserContext("user9", Map("country" -> "New Zealand")))
//      } yield assertTrue(!isEnabled)
//    },
//    test("feature name don't exist") {
//      for {
//        service <- ZIO.service[FeatureService]
//        isEnabled <- service.isEnabled("what", UserContext("user9", Map("what" -> "New Zealand")))
//      } yield assertTrue(!isEnabled)
//    }
  ).provide(InMemoryFeatureService.layer)


}
