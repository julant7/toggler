import zio.schema.Schema

object JsonCodec {
  def jsonCodec[A](schema: Schema[A]): zio.json.JsonCodec[A] = ???
}
