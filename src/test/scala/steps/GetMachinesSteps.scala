package steps

import cats.data.OptionT
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.cucumber.scala.{EN, ScalaDsl}
import steps.helpers.Validator._
import steps.helpers.{Action, PrerequisiteException}
import steps.helpers.MachineDao._

class GetMachinesSteps extends ScalaDsl with EN {

  private var action: Option[Action[List[MachineState]]] = None

  When("""checking the statuses of the candy machines in the park""") { () =>
    action = Some(Action((_, app) => {
      OptionT(getMachines(app)).map(r => (r.value, r)).value
    }))
  }

  Then("""the status of the candy machines should be returned, sorted by id""") { () =>
    val theAction = action.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
    validate(theAction) { (prevAppState, machineAndOutput, nextAppState) =>
      stateUnChanged(prevAppState, nextAppState) && machineAndOutput._2.value == prevAppState.store.values.toList.sortBy(_.id)
    }
  }

}