package http

import controller.routes
import dto.{AddFlagRequest, AddFlagResponse, CheckRequest, CheckResponse, GetFlagsResponse}
import entity.{AlwaysOn, Rule}
import layer.TestDbHelper
import service.TestFeatureService.testDbHelperLayer
import services.{DbConnector, FeatureService, FeatureServiceImpl}
import zio.{Scope, ZIO}
import zio.http.*
import zio.test.{test, *}
import zio.json.*
import zio.test.TestAspect.{before, beforeAll}

object TestFeatureController extends ZIOSpecDefault {
  val TEST_DB_CONFIG_PATH = "src/test/resources/application.conf"
  private val dbConnectorLayer = DbConnector.layer(TEST_DB_CONFIG_PATH)
  private val featureServiceImpl = dbConnectorLayer >>> FeatureServiceImpl.layer
  private val testDbHelperLayer = dbConnectorLayer >>> TestDbHelper.layer
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("http tests")(
    test("check should be true") {
      val routesChunk = routes.routes
      val key = "feature-test"
      val addFlagRequest = AddFlagRequest(key = key, rules = List(Rule(AlwaysOn())))
      val checkRequest = CheckRequest(userId = "user7", attributes = Map("rkerke" -> "ewkkwe"))
      for {
        client <- ZIO.service[Client]
        _ <- TestClient.addRoutes(routesChunk.head, routesChunk.tail : _*)
        addResponse <- client.batched(Request.post(URL.root / "flags", Body.json(addFlagRequest)))
        checkResponse <- client.batched(Request.post(URL.root / "check", Body.json(checkRequest)).addQueryParam("featureKey", key))
        checkResponseBody <- checkResponse.body.asString.map(_.fromJson[CheckResponse])
      } yield {
        checkResponseBody match {
          case Left(message) =>
            assertNever("Invalid response from server")
          case Right(body) =>
            assertTrue(body.isEnabled)
        }
      }
    },
    test("get all should contains new flag") {
      val routesChunk = routes.routes
      val key = "feature-test"
      val addFlagRequest = AddFlagRequest(key = key, rules = List(Rule(AlwaysOn())))
      val checkRequest = CheckRequest(userId = "user7", attributes = Map("rkerke" -> "ewkkwe"))
      for {
        client <- ZIO.service[Client]
        _ <- TestClient.addRoutes(routesChunk.head, routesChunk.tail : _*)
        addResponse <- client.batched(Request.post(URL.root / "flags", Body.json(addFlagRequest)))
        getResponse <- client.batched(Request.get(URL.root / "flags"))
        getResponseBody <- getResponse.body.asString.map(_.fromJson[GetFlagsResponse])
      } yield {
        getResponseBody match {
          case Left(message) =>
            assertNever("Invalid response from server")
          case Right(getResponseBody) =>
            assertTrue(getResponseBody.flags.exists(_.key == key))
        }
      }
    },
    test("get all shouldn't contains deleted flag") {
      val routesChunk = routes.routes
      val key = "feature-test"
      val addFlagRequest = AddFlagRequest(key = key, rules = List(Rule(AlwaysOn())))
      val checkRequest = CheckRequest(userId = "user7", attributes = Map("rkerke" -> "ewkkwe"))
      for {
        client <- ZIO.service[Client]
        _ <- TestClient.addRoutes(routesChunk.head, routesChunk.tail : _*)
        addResponse <- client.batched(Request.post(URL.root / "flags", Body.json(addFlagRequest)))
        _ <- client.batched(Request.delete(URL.root / "flags").addQueryParam("featureKey", key))
        getResponse <- client.batched(Request.get(URL.root / "flags"))
        getResponseBody <- getResponse.body.asString.map(_.fromJson[GetFlagsResponse])
      } yield {
        getResponseBody match {
          case Left(message) =>
            assertNever("Invalid response from server")
          case Right(getResponseBody) =>
            assertTrue(!getResponseBody.flags.exists(_.key == key))
        }
      }
    }
  ).provide(TestClient.layer, featureServiceImpl) @@ before {
    (for {
      service <- ZIO.service[TestDbHelper]
      _ <- service.executeTruncate()
    } yield ()).provide(testDbHelperLayer)
  }
}
