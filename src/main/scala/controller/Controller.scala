package controller

import dto.{AddFlagRequest, CheckRequest, CheckResponse, DeleteFlagRequest, GetFlagsResponse}
import entity.{AlwaysOn, FeatureFlag, Rule}
import service.FeatureService
import zio.*
import zio.json.*
import zio.http.*
import zio.http.Status.{BadRequest, InternalServerError}


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
        case Right(requestBody) => service.upsert(requestBody).map(_ => Response.ok)
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
  Method.POST / "cache" -> handler { (req: Request) =>
    (for {
      service <- ZIO.service[FeatureService]
      _ <- service.updateCache()
    } yield Response.ok).mapError(err => Response.json(err.getMessage).status(InternalServerError))
  },
  Method.DELETE / "flags" -> handler { (req: Request) =>
    val deleteFlagRequest = req.body.asString.map(_.fromJson[DeleteFlagRequest])
    (for  {
      requestBody <- deleteFlagRequest
      service <- ZIO.service[FeatureService]
      _ <- requestBody match {
        case Left(message) => ZIO.succeed(Response.json(message).status(BadRequest))
        case Right(request) => service.delete(request)
      }
    } yield Response.ok).mapError(err => Response.json(err.getMessage).status(InternalServerError))
  },

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
