package entity

import dto.CheckRequest

object RuleEvaluator {
  def evaluate(rule: Rule, request: CheckRequest, key: String): Boolean = {
    rule.condition match {
      case AlwaysOn() =>
        true
      case UserList(userIds) =>
        userIds.contains(request.userId)
      case CountryAllow(countries) => 
        request.attributes.get("country").exists(countries.contains)
      case null =>
        false
    }
  }

}
