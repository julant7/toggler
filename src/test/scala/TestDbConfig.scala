import zio.{Config, ConfigProvider, Task, ZIO}
import zio.*
import zio.config.*
import zio.config.magnolia.deriveConfig
import zio.config.typesafe.*
import zio.config.magnolia.*

import java.io.File

case class TestDbConfig(driverClassName: String, jdbcUrl: String, user: String, password: String)

object TestDbConfig {
  val DEFAULT_DB_CONFIG_PATH = "test/resources/application.conf"
  private val config: Config[TestDbConfig] = deriveConfig[TestDbConfig]

  def refFromFile(fileName: String = DEFAULT_DB_CONFIG_PATH): Task[TestDbConfig] = {
    for {
      file <- ZIO.attempt(new File(fileName))
      source = ConfigProvider.fromHoconFile(file)
      config <- read(config from source)
    } yield config
  }
}
