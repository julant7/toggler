package dto

import entity.Rule
import zio.json.{DeriveJsonCodec, JsonCodec}

case class AddFlagRequest(val key: String, val rules: List[Rule])

object AddFlagRequest {
  implicit val codec: JsonCodec[AddFlagRequest] = DeriveJsonCodec.gen[AddFlagRequest]
}