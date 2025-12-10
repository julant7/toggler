package controller

import dto.{AddFlagRequest, CheckRequest, CheckResponse}
import service.FeatureService
import zio.*
import zio.json.*
import zio.http.*


val routes1: Routes[FeatureService, Response] = Routes(
  Method.POST / "check" -> handler { (req: Request) =>

    val featureKey = req.url.queryParams("featureKey").asString
    val checkRequest = req.body.asString.map(_.fromJson[CheckRequest])

    (for {
      kk <- checkRequest
      service <- ZIO.service[FeatureService]
      isEn <- service.isEnabled(featureKey, kk.getOrElse(null))
    } yield {
      Response.text(isEn.toJson)
    }).orElseFail(Response.error(Status.BadRequest))

  },
//  Method.POST / "flags" -> handler{ (req: Request) =>
//    val addFlagRequest = req.body.asString.map(_.fromJson[AddFlagRequest])
//    for {
//      service <- ZIO.service[FeatureService]
//      addFlag <- service.upsert(addFlagRequest)
//    } yield ZIO.succeed(Response.ok)
//  },

)
//object Controller extends ZIOAppDefault {
//  val routes =
//    Routes (
//      Method.GET / Root -> handler(Response.text("Greetings")),
//      Method.GET / "greet" -> handler { (req: Request) =>
//        val name = req.queryOrElse[String]("name", "world")
//        Response.text(s"Hello $name!")
//      }
//    )
//  def run = Server.serve(routes).provide(Server.default)
//}
