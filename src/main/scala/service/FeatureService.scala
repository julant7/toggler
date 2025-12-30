package service

import dto.{AddFlagRequest, CheckRequest, CheckResponse, GetFlagsResponse}
import zio.{UIO, ZIO}

trait FeatureService {
  def isEnabled(featureKey: String, request: CheckRequest): UIO[CheckResponse]
  
  def upsert(newFeatureFlag: AddFlagRequest): ZIO[Any, Throwable, Unit]
  
  def getAll: UIO[GetFlagsResponse]
  
  def updateCache(): zio.ZIO[Any, Throwable, Unit]
}
