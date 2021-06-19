package steps

import com.github.morotsman.investigate_finagle_service.candy_finch.{Coin, MachineInput, Turn}
import com.twitter.finagle.http.Status
import io.cucumber.scala.{EN, ScalaDsl}
import io.finch.{Application, Input}
import org.scalacheck.Arbitrary
import org.scalatestplus.scalacheck.Checkers.check
import steps.World.spec
import steps.helpers.PrerequisiteException

class MachineInputSteps extends ScalaDsl with EN {

  When("""the customer inserts a coin in a candy machine that has not been added to the park""") { () =>
    spec.add(context => {
      context.copy(insertCoinRequest = Some(id => Input.put(s"/machine/$id/coin")))
    })
  }

  Then("""the coin should be rejected""") { () =>
    spec.validate(context => {
      implicit val app: Arbitrary[TestApp] =
        context.appGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine park generator"))
      check { (app: TestApp) =>
        val shouldBeTrue = for {
          prev <- app.state
          id = prev.id + 1
          result <- app.insertCoin(Input.put(s"/machine/${id}/coin")).output.get
          next <- app.state
        } yield result.status match {
          case Status.NotFound =>
            stateUnChanged(prev, next) && machineUnknown(id, prev)
          case Status.BadRequest =>
            stateUnChanged(prev, next) && machineInWrongState(id, prev, Coin)
          case _ => false
        }

        shouldBeTrue.unsafeRunSync()
      }
    })
  }

  def machineUnknown(id: Int, prev: AppState): Boolean =
    !prev.store.contains(id)

  private def stateUnChanged(prev: AppState, next: AppState): Boolean =
    sameId(prev, next) && storeSame(prev, next)

  private def sameId(prev: AppState, next: AppState): Boolean =
    prev.id == next.id

  private def storeSame(prev: AppState, next: AppState): Boolean =
    prev.store == next.store

  def machineInWrongState(id: Int, prev: AppState, command: MachineInput): Boolean = command match {
    case Turn =>
      prev.store(id).locked || prev.store(id).candies <= 0
    case Coin =>
      !prev.store(id).locked || prev.store(id).candies <= 0
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
}