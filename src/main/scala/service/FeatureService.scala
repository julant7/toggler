package service

import dto.{AddFlagRequest, CheckRequest, CheckResponse}
import zio.{UIO, ZIO}
import zio.http.Response

trait FeatureService {
  def isEnabled(featureKey: String, request: CheckRequest): UIO[CheckResponse]
  
  def upsert(newFeatureFlag: AddFlagRequest): UIO[Unit]
  
  def getAll: UIO[Map[String, entity.FeatureFlag]]
}
