package services

import dto.{AddFlagRequest, AddFlagResponse, CheckRequest, CheckResponse, DeleteFlagRequest, GetFlagsResponse}
import zio.{UIO, ZIO}

trait FeatureService {
  def isEnabled(featureKey: String, request: CheckRequest): UIO[CheckResponse]
  
  def upsert(newFeatureFlag: AddFlagRequest): ZIO[Any, Throwable, AddFlagResponse]
  
  def getAll: UIO[GetFlagsResponse]
  
  def updateCache(): ZIO[Any, Throwable, Unit]
  
  def delete(featureKey: String): ZIO[Any, Throwable, Unit]
}
