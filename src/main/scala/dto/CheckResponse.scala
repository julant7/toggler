package dto

import zio.schema.{DeriveSchema, Schema}

case class CheckResponse(isEnabled: Boolean)

object CheckResponse {
  implicit val schema: Schema[CheckResponse] = DeriveSchema.gen
  implicit val jsonCodec: zio.json.JsonCodec[CheckResponse] =
    zio.schema.codec.JsonCodec.jsonCodec(schema)
}