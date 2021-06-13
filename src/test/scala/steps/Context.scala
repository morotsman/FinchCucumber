package steps

case class Context(
                    number: Option[Int]
                  )

object Context {
  def emptyContext: Context = Context(None)
}
