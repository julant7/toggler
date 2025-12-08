object RuleEvaluator {
  def evaluate(rule: Rule, context: UserContext, key: String): Boolean = {
    rule.condition match {
      case AlwaysOn() => true
      case UserList(userIds) => userIds.contains(context.userId)
      case CountryAllow(countries) => {
        context.attributes.get("country").exists(countries.contains)
      }
    }
  }
}
