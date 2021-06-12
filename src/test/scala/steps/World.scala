package steps

import cats.effect.IO
import io.cucumber.scala.{ScalaDsl, Scenario}

case class Context(
                    number: Option[Int]
                  )

object World {
  val spec: Spec[IO, Context] = Spec[IO, Context](Context(Some(2)))
}

class World extends ScalaDsl {
  import steps.World.spec
  After { scenario: Scenario =>
    spec.run().unsafeRunSync()
    ()
  }
}
