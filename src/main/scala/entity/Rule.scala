package entity

import zio.json.jsonDiscriminator
import zio.schema.{DeriveSchema, Schema}

case class Rule(condition: Condition, result: Boolean = true)

object Rule {
  implicit val schema: Schema[Rule] =
    DeriveSchema.gen
  implicit val jsonCodec: zio.json.JsonCodec[Rule] =
    zio.schema.codec.JsonCodec.jsonCodec(schema)
}

@jsonDiscriminator("type") sealed trait Condition

object Condition {
  implicit val schema: Schema[Rule] =
    DeriveSchema.gen
  implicit val jsonCodec: zio.json.JsonCodec[Rule] =
    zio.schema.codec.JsonCodec.jsonCodec(schema)
}

case class AlwaysOn() extends Condition

case class UserList(userIds: Set[String]) extends Condition

case class CountryAllow(countries: Set[String]) extends Condition