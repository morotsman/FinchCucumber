package steps

import cats.data.OptionT
import cats.implicits.{catsStdInstancesForOption, toTraverseOps}
import com.github.morotsman.investigate_finagle_service.candy_finch.{Coin, MachineInput, Turn}
import com.twitter.finagle.http.Status
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.cucumber.scala.{EN, ScalaDsl}
import io.finch.{Application, Input}
import org.scalatestplus.scalacheck.Checkers.check
import io.circe.generic.auto._
import io.finch.circe._
import org.scalacheck.Arbitrary
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.Checkers._
import steps.helpers.PrerequisiteException

class MachineInputSteps extends ScalaDsl with EN {

  When("""the customer inserts a coin in a candy machine that has not been added to the park""") { () =>
    World.context = World.context.copy(request =
        Some((machine, testApp) => appState => {
          val unknownId = appState.id + 1
          val insertCoinRequest = Input.put(s"/machine/$unknownId/coin")
          val result = OptionT(testApp.insertCoin(insertCoinRequest).output.sequence)
          result.map(om => (machine.withId(unknownId), om)).value
        }))
  }

  When("""a coin is inserted in the candy machine""") {
    World.context = World.context.copy(request =
      Some(value = (machine, testApp) => _ => {
        val createMachineRequest = Input.post("/machine").withBody[Application.Json]
        val createMachine = OptionT(testApp.createMachine(createMachineRequest(machine)).output.sequence)

        val insertCoinRequest = (id: Int) => Input.put(s"/machine/$id/coin")
        val insertCoinInMachine = (id: Int) => OptionT(testApp.insertCoin(insertCoinRequest(id)).output.sequence)

        (for {
          machine <- createMachine
          result <- insertCoinInMachine(machine.value.id)
        } yield (machine.value, result)).value
      }))
  }


  Then("""the coin should be rejected""") { () =>
    validate(World.context)
  }

  Then("""the candy machine should be unlocked""") { () =>
    validate(World.context)
  }

  def validate(context: Context): Assertion = {
    implicit val machine: Arbitrary[MachineWithoutId] =
      context.machineGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine generator"))
    implicit val app: Arbitrary[TestApp] =
      context.appGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine park generator"))
    val request =
      context.request.getOrElse(throw new PrerequisiteException("Expecting a finch action"))

    check { (app: TestApp, randomMachine: MachineWithoutId) =>
      val result = for {
        prevAppState <- app.state
        machineAndOutput <- request(randomMachine, app)(prevAppState)
        nextAppState <- app.state
      } yield machineAndOutput.map(mo => mo._2.status match {
        case Status.NotFound =>
          stateUnChanged(prevAppState, nextAppState) && machineUnknown(mo._1.id, prevAppState)
        case Status.BadRequest =>
          stateUnChanged(addMachineToState(mo._1, prevAppState), nextAppState) && machineInWrongState(mo._1.id, nextAppState, Coin)
        case Status.Ok =>
          isUnlocked(mo._1.id, addMachineToState(mo._1, prevAppState), nextAppState)
        case _ =>
          false
      })
      result.unsafeRunSync()
        .getOrElse(throw new PrerequisiteException("Could not execute the finch action"))
    }
  }

  def addMachineToState(m: MachineState, app: AppState): AppState =
    app.copy(
      id = app.id + 1,
      store = app.store + (m.id -> m)
    )

  private def machineUnknown(id: Int, prev: AppState): Boolean =
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

  def isUnlocked(id: Int, prevState: AppState, nextState: AppState): Boolean = (for {
    prev <- prevState.store.get(id)
    next <- nextState.store.get(id)
    if prev.locked && !next.locked
    if prev.candies > 0 && next.candies == prev.candies && next.coins == prev.coins + 1
    if sameId(prevState, nextState)
    if prevState.store.filter(kv => kv._1 != id) == nextState.store.filter(kv => kv._1 != id)
  } yield true).getOrElse(false)

}