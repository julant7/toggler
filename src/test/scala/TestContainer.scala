import com.dimafeng.testcontainers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
//import zio.blocking.{effectBlocking, BLocking}
import zio._

class TestContainer()
object TestContainer {
  val layer: ZLayer[Scope, Nothing, PostgreSQLContainer] = ZLayer {
    ZIO
      .attemptBlocking {
        val container = PostgreSQLContainer(dockerImageNameOverride = DockerImageName.parse("postgres:17"))
        container.start()
        container
      }
      .withFinalizerAuto
      .orDie
  }

//  val configuration: ZLayer[PostgreSQLContainer, Nothing, DbConfig] = ZLayer {
//    for { container <- ZIO.service[PostgreSQLContainer] } yield DbConfig(
//      
//    )
//  }

}