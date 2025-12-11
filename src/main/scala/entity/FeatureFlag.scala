package entity

import zio.*
import zio.json.{DeriveJsonCodec, DeriveJsonEncoder, JsonCodec, JsonEncoder}

case class FeatureFlag(val key: String, val rules: List[Rule])

object FeatureFlag {
  implicit val codec: JsonCodec[FeatureFlag] =
    DeriveJsonCodec.gen[FeatureFlag]
}