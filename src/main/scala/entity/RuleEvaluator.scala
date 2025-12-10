package entity

import dto.{AddFlagRequest, CheckRequest, CheckResponse}
import entity.{AlwaysOn, CountryAllow, Rule, UserList}
import zio.http.Response
import zio.{Task, UIO, ZIO}

object RuleEvaluator {
  def evaluate(rule: Rule, request: CheckRequest, key: String): Boolean = {
    rule.condition match {
      case AlwaysOn() => true
      case UserList(userIds) => userIds.contains(request.userId)
      case CountryAllow(countries) => 
        request.attributes.get("country").exists(countries.contains)
    }
  }

}
