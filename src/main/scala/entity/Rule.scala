package entity

import doobie.*
import doobie.postgres.circe.json.implicits.*
import io.circe.*
import io.circe.generic.semiauto.*
import zio.json.{DeriveJsonCodec, DeriveJsonEncoder, JsonCodec, JsonEncoder, jsonDiscriminator, jsonHint}

case class Rule(condition: Condition, result: Boolean = true)

object Rule {
  implicit val codec: JsonCodec[Rule] =
    DeriveJsonCodec.gen[Rule]

  implicit val encoder: Encoder[Rule] = deriveEncoder[Rule]
  implicit val decoder: io.circe.Decoder[Rule] = deriveDecoder[Rule]
}

@jsonDiscriminator("type")
sealed trait Condition

object Condition {
  implicit val codec: JsonCodec[Condition] =
    DeriveJsonCodec.gen[Condition]

  implicit val encoder: Encoder[Condition] = deriveEncoder[Condition]
  implicit val decoder: io.circe.Decoder[Condition] = deriveDecoder[Condition]
  implicit val meta: Meta[Condition] = new Meta(pgDecoderGet, pgEncoderPut)

}


@jsonHint("omena") case class AlwaysOn() extends Condition

case class UserList(userIds: Set[String]) extends Condition

case class CountryAllow(countries: Set[String]) extends Condition