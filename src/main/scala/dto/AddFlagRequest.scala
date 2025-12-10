package dto

import entity.Rule
import zio.schema.*

case class AddFlagRequest(val key: String, val rules: List[Rule])

object AddFlagRequest {
  implicit val schema: Schema[AddFlagRequest] = {
    DeriveSchema.gen
  }
  implicit val jsonCodec: zio.json.JsonCodec[AddFlagRequest] =
    zio.schema.codec.JsonCodec.jsonCodec(schema)
}