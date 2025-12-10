package service

import dto.{AddFlagRequest, CheckRequest, CheckResponse}
import zio.http.Response
import zio.{IO, Task, UIO, ZIO, ZNothing}

trait FeatureService {
  def isEnabled(featureKey: String, request: CheckRequest): ZIO[Any, Response, CheckResponse]

  def isEnabled1(featureKey: String, request: Task[CheckRequest]): ZIO[Any, Response, CheckResponse]
  
  def upsert(featureFlag: ZIO[Any, Response, AddFlagRequest]): UIO[Unit]
}
