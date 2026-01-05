package entity

import java.sql.Timestamp

case class FeatureFlag(flag_id: Int, key: String, rules: List[Rule], created_at: Timestamp, updated_at: Timestamp, is_deleted: Boolean)
