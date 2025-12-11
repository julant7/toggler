import controller.routes
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
    Server.serve(routes).provide(InMemoryFeatureService.layer, Server.defaultWithPort(8090))

  }
}
