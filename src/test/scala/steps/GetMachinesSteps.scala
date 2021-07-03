package steps

import cats.data.OptionT
import cats.implicits.{catsStdInstancesForOption, toTraverseOps}
import io.cucumber.scala.{EN, ScalaDsl}
import io.finch.Input
import org.scalatestplus.scalacheck.Checkers.check
import org.scalacheck.Arbitrary
import org.scalatestplus.scalacheck.Checkers._
import steps.helpers.PrerequisiteException

class GetMachinesSteps extends ScalaDsl with EN {

  When("""checking the statuses of the candy machines in the park""") { () =>
    World.context = World.context.copy(getMachinesRequest = Some( (machine, app) => {
      val input = Input.get("/machine")
      val result = OptionT(app.getMachines(input).output.sequence)
      result.map(r => (r.value, r)).value
    }))
  }

  Then("""the status of the candy machines should be returned, sorted by id""") { () =>
    implicit val machine: Arbitrary[MachineWithoutId] =
      World.context.machineGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine generator"))
    implicit val app: Arbitrary[TestApp] =
      World.context.appGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine park generator"))
    val request =
      World.context.getMachinesRequest.getOrElse(throw new PrerequisiteException("Expecting a finch action"))

    check { (machineToAdd: MachineWithoutId, app: TestApp) =>
      val shouldBeTrue = for {
        prev <- app.state
        machines <- request(machineToAdd, app)
        next <- app.state
      } yield machines.map(ms =>
        stateUnChanged(prev, next) && ms._2.value == prev.store.values.toList.sortBy(_.id)
      )

      shouldBeTrue.unsafeRunSync()
        .getOrElse(throw new PrerequisiteException("Could not execute the finch action"))
    }

  }

  private def stateUnChanged(prev: AppState, next: AppState): Boolean =
    sameId(prev, next) && storeSame(prev, next)

  private def sameId(prev: AppState, next: AppState): Boolean =
    prev.id == next.id

  private def storeSame(prev: AppState, next: AppState): Boolean =
    prev.store == next.store
}