package service

import dto.{AddFlagRequest, DeleteFlagRequest}
import entity.{AlwaysOn, Condition, Rule, UserList}
import layer.TestDbHelper
import services.{DbConnector, FeatureServiceImpl}
import zio.{Scope, ZIO}
import zio.test.*
import zio.test.TestAspect.beforeAll

object TestFeatureService extends ZIOSpecDefault {
  val TEST_DB_CONFIG_PATH = "src/test/resources/application.conf"
  private val dbConnectorLayer = DbConnector.layer(TEST_DB_CONFIG_PATH)
  private val featureServiceImpl = dbConnectorLayer >>> FeatureServiceImpl.layer
  private val testDbHelperLayer = dbConnectorLayer >>> TestDbHelper.layer
  override def spec: Spec[TestEnvironment & Scope, Throwable] = suite("operations with features")(
    test("insert 1 feature") {
      val key = "feature5"
      val addFlagRequest = AddFlagRequest(key = key, rules = List(Rule(condition = AlwaysOn())))
      val zio =
        for {
          service <- ZIO.service[FeatureServiceImpl]
          flagId <- service.upsert(addFlagRequest)
          getFlagsResponse <- service.getAll
        } yield {
          val insertedFlag = getFlagsResponse.flags.filter(_.key == key).head
          insertedFlag.flagId == flagId && insertedFlag.key == addFlagRequest.key && insertedFlag.rules == addFlagRequest.rules
        }

      assertZIO(zio)(Assertion.equalTo(true))
      Assertion.equalTo(true)
    },
    test("update existing feature") {
      val key = "feature5"
      val addFirstFlagRequest = AddFlagRequest(key = key, rules = List(Rule(condition = AlwaysOn())))
      val addSecondFlagRequest = AddFlagRequest(key = key, rules = List(Rule(condition = UserList(userIds = Set("user1", "user2")))))
      val zio =
        for {
          service <- ZIO.service[FeatureServiceImpl]
          flagId <- service.upsert(addFirstFlagRequest)
          insertedFlagId <- service.upsert(addSecondFlagRequest)
          getFlagsResponse <- service.getAll
        } yield {
          val insertedFlag = getFlagsResponse.flags.filter(_.key == key).head
          insertedFlag.flagId == flagId && insertedFlag.key == addSecondFlagRequest.key && insertedFlag.rules == addSecondFlagRequest.rules
        }
      assertZIO(zio)(Assertion.equalTo(true))
    },
    test("delete non-existent feature") {
      val key = "feature3"
      val zio =
        for {
          service <- ZIO.service[FeatureServiceImpl]
          _ <- service.delete(DeleteFlagRequest("feature3"))
          getFlagsResponse <- service.getAll
        } yield {
          getFlagsResponse.flags.find(_.key == key)
        }
      assertZIO(zio)(Assertion.equalTo(None))
    },
    test("delete existent feature") {
      val key = "feature3"
      val zio =
        for {
          service <- ZIO.service[FeatureServiceImpl]
          _ <- service.upsert(AddFlagRequest(key = "feature3", rules = List(Rule(condition = AlwaysOn()))))
          _ <- service.delete(DeleteFlagRequest("feature3"))
          getFlagsResponse <- service.getAll
        } yield {
          getFlagsResponse.flags.find(_.key == key)
        }
      assertZIO(zio)(Assertion.equalTo(None))
    }
  ).provide(featureServiceImpl) @@ beforeAll{
    (for {
      service <- ZIO.service[TestDbHelper]
      _ <- service.executeTruncate()
    } yield ()).provide(testDbHelperLayer)
  }
//
}
