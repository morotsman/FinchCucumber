package steps

import cats.effect.IO
import io.cucumber.scala.{ScalaDsl, Scenario}
import steps.helpers.Spec

object World {
  private val initialContext = Context(Some(2))
  val spec: Spec[IO, Context] = Spec(initialContext)
}

class World extends ScalaDsl {
  import steps.World.spec
  After { scenario: Scenario =>
    spec.run().unsafeRunSync()
    ()
  }
}
