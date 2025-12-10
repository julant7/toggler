package entity

import entity.Rule
import zio.*
import zio.http.*
import zio.schema.*

case class FeatureFlag(val key: String, val rules: List[Rule])
