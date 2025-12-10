package dto

import zio.json._

case class CheckRequest(val userId: String, val attributes: Map[String, String])

object CheckRequest {

  implicit val codec: JsonCodec[CheckRequest] = DeriveJsonCodec.gen[CheckRequest]
}