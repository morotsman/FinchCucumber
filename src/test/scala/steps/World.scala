package steps

import cats.effect.IO
import io.cucumber.scala.{ScalaDsl, Scenario}
import steps.helpers.Spec

object World {
  var spec: Spec[IO, Context] = Spec(Context.emptyContext)
}

class World extends ScalaDsl {

  import steps.World.spec

  Before { _: Scenario =>
    spec = Spec(Context.emptyContext)
  }
}
