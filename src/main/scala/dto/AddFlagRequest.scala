package dto

import entity.Rule
import zio.json.{DeriveJsonCodec, JsonCodec}
import io.circe.{Decoder, Encoder}
import zio.schema.{DeriveSchema, Schema}

case class AddFlagRequest(key: String, rules: List[Rule])

object AddFlagRequest {
  implicit val codec: JsonCodec[AddFlagRequest] = DeriveJsonCodec.gen[AddFlagRequest]
  implicit val schema: Schema[AddFlagRequest] = DeriveSchema.gen
}