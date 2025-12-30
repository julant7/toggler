package dto

import entity.FeatureFlag
import zio.json.{DeriveJsonCodec, JsonCodec}

case class GetFlagsResponse(flags: List[GetFlagResponse])

object GetFlagsResponse {
  implicit val codec: JsonCodec[GetFlagsResponse] = DeriveJsonCodec.gen[GetFlagsResponse]


}
