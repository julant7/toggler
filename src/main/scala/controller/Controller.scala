package controller

import dto.*
import services.FeatureService
import zio.*
import zio.http.*
import zio.http.Status.{BadRequest, InternalServerError}
import zio.json.*

//object Controller {
  val routes: Routes[FeatureService, Response] = Routes(
    Method.POST / "check" -> handler { (req: Request) =>

      val featureKey = req.url.queryParams("featureKey").asString
      val checkRequest = req.body.asString.map(_.fromJson[CheckRequest])

      (for {
        request <- checkRequest
        service <- ZIO.service[FeatureService]
        isEnabled <- request match {
          case Left(message) => ZIO.succeed(Response.json(message).status(Status.BadRequest))
          case Right(requestBody) => service.isEnabled(featureKey, requestBody).map(el => Response.text(el.toJson))
        }
      } yield isEnabled).mapError(err => Response.status(Status.InternalServerError))
    },
    Method.POST / "flags" -> handler { (req: Request) =>
      val addFlagRequest = req.body.asString.map(_.fromJson[AddFlagRequest])
      (for {
        request <- addFlagRequest
        service <- ZIO.service[FeatureService]
        result <- request match {
          case Left(message) => ZIO.succeed(Response.json(message).status(Status.BadRequest))
          case Right(requestBody) => service.upsert(requestBody).map(el => Response.json(el.toJson))
        }
      } yield result).mapError(err => {
        Response.json(err.getMessage).status(Status.InternalServerError)
      })
    },
    Method.GET / "flags" -> handler { (req: Request) =>
      for {
        service <- ZIO.service[FeatureService]
        result <- service.getAll
      } yield Response.text(result.toJson)
    },
    Method.DELETE / "flags" -> handler { (req: Request) =>
      val featureKey = req.url.queryParams("featureKey").asString
      (for {
        service <- ZIO.service[FeatureService]
        _ <- service.delete(featureKey)
      } yield Response.ok).mapError(err => Response.json(err.getMessage).status(InternalServerError))
    },
    Method.POST / "rerer" -> handler { (req: Request) => {
      ZIO.succeed(Response.ok)
    }}

  )
//}

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
