import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object Main extends ZIOAppDefault {

  val environment = (InMemoryFeatureService.layer).build
  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = {
    for {
      service <- environment.map(_.get[FeatureService])
      isEnabled <- service.isEnabled("priv", UserContext("124", Map("priv" -> "priv")))
      _ <- ZIO.succeed(assert(!isEnabled))
    } yield ()
//    val jj = inMemoryFeatureService.flatMap(_.isEnabled("priv", null))
//    ZIO.succeed(assert(jj.))
//    val isEnabled = inMemoryFeatureService.
  }
}
