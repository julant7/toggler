case class Rule(condition: Condition, result: Boolean = true)

sealed trait Condition

case class AlwaysOn() extends Condition

case class UserList(userIds: Set[String]) extends Condition

case class CountryAllow(countries: Set[String]) extends Condition