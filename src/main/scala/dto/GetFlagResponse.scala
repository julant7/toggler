package dto

import entity.Rule
import zio.json.{DeriveJsonCodec, JsonCodec}

case class GetFlagResponse(flagId: Int, key: String, rules: List[Rule], created_at: String, updated_at: String)

object GetFlagResponse {
  implicit val codec: JsonCodec[GetFlagResponse] = DeriveJsonCodec.gen[GetFlagResponse]
}
