import zio.UIO

trait FeatureService {
  def isEnabled(featureKey: String, context: UserContext): UIO[Boolean]
}
