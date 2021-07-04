package steps

import cats.data.OptionT
import cats.implicits.{catsStdInstancesForOption, toTraverseOps}
import com.github.morotsman.investigate_finagle_service.candy_finch.{Coin, MachineInput, Turn}
import com.twitter.finagle.http.Status
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.cucumber.scala.{EN, ScalaDsl}
import io.finch.{Application, Input}
import io.circe.generic.auto._
import io.finch.circe._
import steps.Validator._
import steps.helpers.PrerequisiteException
import steps.MachineDao._

class MachineInputSteps extends ScalaDsl with EN {

  private var action: Option[Action[MachineState]] = None

  When("""the customer inserts a coin in a candy machine that has not been added to the park""") { () =>
    action =
      Some(Action((machine, testApp) => {
        val unknownId = -1
        OptionT(insertCoin(testApp, unknownId)).map(om => (machine.withId(unknownId), om)).value
      }))
  }

  When("""a coin is inserted in the candy machine""") {
    action =
      Some(Action((machine, testApp) => {
        (for {
          machine <- OptionT(createMachine(testApp, machine))
          result <- OptionT(insertCoin(testApp, machine.value.id))
        } yield (machine.value, result)).value
      }))
  }

  Then("""the coin should be rejected""") { () =>
    val theAction = action.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
    validate(theAction) { (prevAppState, machineAndOutput, nextAppState) =>
      machineAndOutput._2.status match {
        case Status.NotFound =>
          stateUnChanged(prevAppState, nextAppState) && machineUnknown(machineAndOutput._1.id, prevAppState)
        case Status.BadRequest =>
          stateUnChanged(addMachineToState(machineAndOutput._1, prevAppState), nextAppState) && machineInWrongState(machineAndOutput._1.id, nextAppState, Coin)
        case _ =>
          false
      }
    }
  }

  Then("""the candy machine should be unlocked""") { () =>
    val theAction = action.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
    validate(theAction) { (prevAppState, machineAndOutput, nextAppState) =>
      machineAndOutput._2.status match {
        case Status.Ok =>
          isUnlocked(machineAndOutput._1.id, addMachineToState(machineAndOutput._1, prevAppState), nextAppState)
        case _ =>
          false
      }
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