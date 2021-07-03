package steps

import io.cucumber.scala.{ScalaDsl, Scenario}
import steps.World.context

object World {
  var context: Context = Context()
}

class World extends ScalaDsl {
  Before { _: Scenario =>
    context =  Context()
  }
}
