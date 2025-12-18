package entity

import zio.json.{DeriveJsonCodec, JsonCodec}

case class FeatureFlag(id: Int, key: String, rules: List[Rule])

object FeatureFlag {

  implicit val codec: JsonCodec[FeatureFlag] = DeriveJsonCodec.gen[FeatureFlag]
}
