package steps

import cats.effect.IO
import io.cucumber.scala.{ScalaDsl, Scenario}
import steps.helpers.Spec

object World {
  val spec: Spec[IO, Context] = Spec(Context.emptyContext)
}

class World extends ScalaDsl {
  import steps.World.spec
  After { scenario: Scenario =>
    spec.run().unsafeRunSync()
    ()
  }
}
