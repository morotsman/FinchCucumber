package steps

import cats.data.OptionT
import cats.implicits.{catsStdInstancesForOption, toTraverseOps}
import io.cucumber.scala.{EN, ScalaDsl}
import io.finch.Input
import steps.Validator._

class GetMachinesSteps extends ScalaDsl with EN {

  When("""checking the statuses of the candy machines in the park""") { () =>
    World.context = World.context.copy(getMachinesRequest = Some(Action((machine, app) => {
      val input = Input.get("/machine")
      val result = OptionT(app.getMachines(input).output.sequence)
      result.map(r => (r.value, r)).value
    })))
  }

  Then("""the status of the candy machines should be returned, sorted by id""") { () =>
    validateListAction { (prevAppState, machineAndOutput, nextAppState) =>
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