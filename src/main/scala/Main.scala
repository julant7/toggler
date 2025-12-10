import controller.routes1
import dto.{CheckRequest, CheckResponse}
import service.{FeatureService, InMemoryFeatureService}
import zio.*
import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.{Endpoint, EndpointExecutor}

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
  def endpointExecutor(client: Client) = EndpointExecutor(client, url"https://localhost:8080")
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = {
//    val clientApp: ZIO[Scope with Client, Nothing, CheckResponse] = for {
//      client <- ZIO.service[Client]
//      response <- endpointExecutor(client)(endpoint("priv", CheckRequest("7", Map("priv" -> "priv"))))
//    } yield response
    Server.serve(routes1).provide(InMemoryFeatureService.layer, Server.default)
//    Server.serve(routes1).provide(InMemoryFeatureService.layer, Server.default)
//    Server.serve(routes).provide(Server.default, InMemoryFeatureService.layer)
//    for {
//      service <- environment.map(_.get[FeatureService])
//      isEnabled <- service.isEnabled("priv", entity.UserContext("124", Map("priv" -> "priv")))
//      _ <- ZIO.succeed(assert(!isEnabled))
//    } yield ()
//    val jj = inMemoryFeatureService.flatMap(_.isEnabled("priv", null))
//    ZIO.succeed(assert(jj.))
//    val isEnabled = inMemoryFeatureService.
  }
}
