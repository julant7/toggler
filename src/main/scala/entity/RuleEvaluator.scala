package entity

import dto.CheckRequest

object RuleEvaluator {
  def evaluate(rule: Rule, request: CheckRequest, key: String): Boolean = {
    rule.condition match {
      case AlwaysOn() => {
        println(1)
        true
      }
      case UserList(userIds) => {
        println(2)
        userIds.contains(request.userId)
      }
      case CountryAllow(countries) => 
        println(3)
        request.attributes.get("country").exists(countries.contains)
    }
  }

}
