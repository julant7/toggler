package dto

import zio.json.*
import zio.schema.{DeriveSchema, Schema}

case class CheckRequest(val userId: String, val attributes: Map[String, String])

object CheckRequest {
  implicit val schema: Schema[CheckRequest] = DeriveSchema.gen[CheckRequest]
  implicit val codec: JsonCodec[CheckRequest] = DeriveJsonCodec.gen[CheckRequest]
}