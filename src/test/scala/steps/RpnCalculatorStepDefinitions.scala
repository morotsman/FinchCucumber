package steps

import com.github.morotsman.investigate_finagle_service.candy_finch.RpnCalculator
import io.cucumber.scala.{EN, ScalaDsl, Scenario}
import org.junit.Assert._
import steps.World.spec


class RpnCalculatorStepDefinitions extends ScalaDsl with EN {

  val calc = new RpnCalculator

  When("""I add {double} and {double}""") { (arg1: Double, arg2: Double) =>
    spec.map(a => {
      println(a)
      calc push arg1
      calc push arg2
      calc push "+"
      a + 10
    })
  }

  Then("the result is {double}") { (expected: Double) =>
    spec.foreach(a => {
      println(a)
      assertEquals(expected, calc.value, 0.001)
    })
  }

  Before("not @foo") { (scenario: Scenario) =>
    println(s"Runs before scenarios *not* tagged with @foo (${scenario.getId})")
  }
}

