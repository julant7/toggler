package config

import zio.*
import zio.config.*
import zio.config.typesafe.*

import java.io.File
import zio.config.magnolia.*

import java.util.Properties

final case class DbConfig(driverClassName: String, jdbcUrl: String, user: String, password: String)

object DbConfig {
  val DEFAULT_DB_CONFIG_PATH = "src/main/resources/application.conf"
  private val config: Config[DbConfig] = deriveConfig[DbConfig]

  def refFromFile(fileName: String = DEFAULT_DB_CONFIG_PATH): Task[DbConfig] = {
    for {
      file <- ZIO.attempt(new File(fileName))
      source = ConfigProvider.fromHoconFile(file)
      config <- read(config from source)
    } yield config
  }
}

final case class AppConfig(db: DbConfig) {

}

object AppConfig {

  def getProperties: Properties = {
    val prop = new Properties()
    prop.load(Thread.currentThread().getContextClassLoader.getResourceAsStream("application.yaml"))
    prop
  }

//  val layer: zio.ZLayer[Any, Throwable, zio.Ref[DbConfig]] = ZLayer.fromZIO(DbConfig.refFromFile(""))

}
