import controller.routes
import dto.{CheckRequest, CheckResponse}
import entity.{AlwaysOn, Rule}
import service.{FeatureService, InMemoryFeatureService, PostgresFeatureService}
import zio.*
import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.{Endpoint, EndpointExecutor}
import zio.json.EncoderOps

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
  val environment = (InMemoryFeatureService.layer).build
  def endpointExecutor(client: Client) = EndpointExecutor(client, url"https://localhost:8090")
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = {
//    val clientApp: ZIO[Scope with Client, Nothing, CheckResponse] = for {
//      client <- ZIO.service[Client]
//      response <- endpointExecutor(client)(endpoint("priv", CheckRequest("7", Map("priv" -> "priv"))))
//    } yield response
//    println(List(Rule(condition = AlwaysOn())).toJson)
    Server.serve(routes).provide(PostgresFeatureService.layer, Server.defaultWithPort(8090))
  }
}
