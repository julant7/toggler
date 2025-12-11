package entity

import zio.json.{DeriveJsonCodec, DeriveJsonEncoder, JsonCodec, JsonEncoder, jsonDiscriminator, jsonHint, jsonNoExtraFields}
import zio.schema.{DeriveSchema, Schema}

case class Rule(condition: Condition, result: Boolean = true)

object Rule {
  implicit val codec: JsonCodec[Rule] =
    DeriveJsonCodec.gen[Rule]
}

@jsonDiscriminator("type")
sealed trait Condition

object Condition {
  implicit val codec: JsonCodec[Condition] =
    DeriveJsonCodec.gen[Condition]
}


@jsonHint("omena") case class AlwaysOn() extends Condition

case class UserList(userIds: Set[String]) extends Condition

case class CountryAllow(countries: Set[String]) extends Condition