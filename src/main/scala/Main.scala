import controller.routes
import dto.{CheckRequest, CheckResponse}
import entity.{AlwaysOn, Rule}
import service.{DbConnector, FeatureService, PostgresFeatureService}
import zio.*
import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.{Endpoint, EndpointExecutor}
import zio.json.EncoderOps

import cats.effect.Async
import cats.implicits.*

object Main extends ZIOAppDefault {

//  val endpoint =
//    Endpoint(Method.POST / "check" / string("featureKey"))
//      .query(HttpCodec.query[CheckRequest])
//      .out[CheckResponse]

//  val routes =
//    endpoint.implement({ case (featureKey, userContext) =>
//      for {
//        service <- ZIO.service[FeatureService]
//        isEnabled <- service.isEnabled(featureKey, userContext)
//      } yield isEnabled
//    })
  def endpointExecutor(client: Client) = EndpointExecutor(client, url"https://localhost:8090")
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = {
//    val clientApp: ZIO[Scope with Client, Nothing, CheckResponse] = for {
//      client <- ZIO.service[Client]
//      response <- endpointExecutor(client)(endpoint("priv", CheckRequest("7", Map("priv" -> "priv"))))
//    } yield response
//    println(List(Rule(condition = AlwaysOn())).toJson)
    val postgresFeatureServiceLayer = DbConnector.layer >>> PostgresFeatureService.layer
    
    Server.serve(routes).provide(postgresFeatureServiceLayer, Server.defaultWithPort(8090))
  }
}
