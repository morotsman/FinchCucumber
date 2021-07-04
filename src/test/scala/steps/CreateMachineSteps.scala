package steps

import cats.data.OptionT
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.cucumber.scala.{EN, ScalaDsl}
import steps.helpers.MachineDao.createMachine
import steps.helpers.Validator._
import steps.helpers.{Action, PrerequisiteException}

class CreateMachineSteps extends ScalaDsl with EN {

  private var action: Option[Action[MachineState]] = None

  When("""the candy machine is added to the park""") { () =>
    action = Some(Action((machine, app) => {
      OptionT(createMachine(app, machine)).map(r => (r.value, r)).value
    }))
  }

  Then("""the machine should be allocated an unique id""") { () =>
    val theAction = action.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
    validate(theAction) { (prevAppState, _, result, currentAppState) =>
      prevAppState.id + 1 == currentAppState.id && !prevAppState.store.contains(result.value.id)
    }
  }

  Then("""the machine should be added to the park""") { () =>
    val theAction = action.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
    validate(theAction) { (prevAppState, _, result, currentAppState) =>
      prevAppState.store + (prevAppState.id -> result.value) == currentAppState.store
    }
  }
}