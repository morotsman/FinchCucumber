package steps

import io.cucumber.scala.{EN, ScalaDsl}

class MachineInputSteps extends ScalaDsl with EN {

  When("""the customer inserts a coin in a unknown candy machine""") { () =>
    // Write code here that turns the phrase above into concrete actions
    throw new io.cucumber.scala.PendingException()
  }
  Then("""the customer should be notified about the problem""") { () =>
    // Write code here that turns the phrase above into concrete actions
    throw new io.cucumber.scala.PendingException()
  }
  When("""a coin is inserted in a locked candy machine""") { () =>
    // Write code here that turns the phrase above into concrete actions
    throw new io.cucumber.scala.PendingException()
  }
  Then("""the candy machine should be unlocked""") { () =>
    // Write code here that turns the phrase above into concrete actions
    throw new io.cucumber.scala.PendingException()
  }
  When("""a coin is inserted in a unlocked candy machine""") { () =>
    // Write code here that turns the phrase above into concrete actions
    throw new io.cucumber.scala.PendingException()
  }
  Then("""the coin should be rejected""") { () =>
    // Write code here that turns the phrase above into concrete actions
    throw new io.cucumber.scala.PendingException()
  }
}