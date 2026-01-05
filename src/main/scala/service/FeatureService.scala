package service

import dto.{AddFlagRequest, CheckRequest, CheckResponse, DeleteFlagRequest, GetFlagsResponse}
import zio.{UIO, ZIO}

trait FeatureService {
  def isEnabled(featureKey: String, request: CheckRequest): UIO[CheckResponse]
  
  def upsert(newFeatureFlag: AddFlagRequest): ZIO[Any, Throwable, Unit]
  
  def getAll: UIO[GetFlagsResponse]
  
  def updateCache(): ZIO[Any, Throwable, Unit]
  
  def delete(deletingFlag: DeleteFlagRequest): ZIO[Any, Throwable, Unit]
}
