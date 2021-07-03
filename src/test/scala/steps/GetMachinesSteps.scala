package steps

import cats.data.OptionT
import cats.implicits.{catsStdInstancesForOption, toTraverseOps}
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.cucumber.scala.{EN, ScalaDsl}
import io.finch.Input
import steps.Validator._
import steps.helpers.PrerequisiteException

class GetMachinesSteps extends ScalaDsl with EN {

  private var action: Option[Action[List[MachineState]]] = None

  When("""checking the statuses of the candy machines in the park""") { () =>
    action = Some(Action((_, app) => {
      val input = Input.get("/machine")
      val result = OptionT(app.getMachines(input).output.sequence)
      result.map(r => (r.value, r)).value
    }))
  }

  Then("""the status of the candy machines should be returned, sorted by id""") { () =>
    val theAction = action.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
    validate(theAction) { (prevAppState, machineAndOutput, nextAppState) =>
      stateUnChanged(prevAppState, nextAppState) && machineAndOutput._2.value == prevAppState.store.values.toList.sortBy(_.id)
    }
  }

  private def stateUnChanged(prev: AppState, next: AppState): Boolean =
    sameId(prev, next) && storeSame(prev, next)

  private def sameId(prev: AppState, next: AppState): Boolean =
    prev.id == next.id

  private def storeSame(prev: AppState, next: AppState): Boolean =
    prev.store == next.store
}