package steps

import io.cucumber.scala.{ScalaDsl, Scenario}

object World {
  val spec: Context[Int] = Context[Int](2)
}

class World extends ScalaDsl {
  import steps.World.spec
  After { scenario: Scenario =>
    spec.run()
  }
}
