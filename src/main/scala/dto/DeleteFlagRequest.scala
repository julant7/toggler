package dto

import zio.http.endpoint.openapi.JsonSchema
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

case class DeleteFlagRequest(key: String)

object DeleteFlagRequest {
  implicit val codec: JsonCodec[DeleteFlagRequest] = DeriveJsonCodec.gen[DeleteFlagRequest]
  implicit val schema: Schema[CheckResponse] = DeriveSchema.gen
}