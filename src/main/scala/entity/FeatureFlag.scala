package entity

import zio.json.{DeriveJsonCodec, DeriveJsonEncoder, JsonCodec, JsonEncoder}

import java.sql.Timestamp
case class FeatureFlag(flag_id: Int, key: String, rules: List[Rule], created_at: Timestamp, updated_at: Timestamp)

object FeatureFlag {
//  implicit val encoderTimestaml: JsonEncoder[Timestamp] = DeriveJsonEncoder.gen[Timestamp]
//  implicit val codecTimestamp: JsonCodec[Timestamp] = DeriveJsonCodec.gen[Timestamp]
//  implicit val mirrorOf: JsonCodec[scala.deriving.Mirror.Of[java.sql.Timestamp]] = DeriveJsonCodec.gen[scala.deriving.Mirror.Of[java.sql.Timestamp]]
//  implicit val codecEntity: JsonCodec[FeatureFlag] = DeriveJsonCodec.gen[FeatureFlag]
}
