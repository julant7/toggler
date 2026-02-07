package dto

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

case class AddFlagResponse(id: Int)

object AddFlagResponse {
  implicit val schema: Schema[AddFlagResponse] = DeriveSchema.gen
  implicit val jsonCodec: JsonCodec[AddFlagResponse] = DeriveJsonCodec.gen
}