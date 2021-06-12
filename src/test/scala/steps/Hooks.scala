package steps

import io.cucumber.scala.{ScalaDsl, Scenario}
import org.junit.Assert.fail


class Hooks extends ScalaDsl{

  After { scenario : Scenario =>
    println(scenario)
    println(scenario.getName)
    println(scenario.getStatus)
    // Do something after each scenario
    // Must return Unit
  }

}