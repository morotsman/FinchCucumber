package steps

import cats.data.OptionT
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.cucumber.scala.{EN, ScalaDsl}
import steps.MachineDao.createMachine
import steps.Validator._
import steps.helpers.PrerequisiteException

class CreateMachineSteps extends ScalaDsl with EN {

  private var action: Option[Action[MachineState]] = None

  When("""the candy machine is added to the park""") { () =>
    action = Some(Action((machine, app) => {
      OptionT(createMachine(app, machine)).map(r => (r.value, r)).value
    }))
  }

  Then("""the machine should be allocated an unique id""") { () =>
    val theAction = action.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
    validate(theAction) { (prevAppState, machineAndOutput, nextAppState) =>
      prevAppState.id + 1 == nextAppState.id && !prevAppState.store.contains(machineAndOutput._2.value.id)
    }
  }

  Then("""the machine should be added to the park""") { () =>
    val theAction = action.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
    validate(theAction) { (prevAppState, machineAndOutput, nextAppState) =>
      prevAppState.store + (prevAppState.id -> machineAndOutput._2.value) == nextAppState.store
    }
  }
}