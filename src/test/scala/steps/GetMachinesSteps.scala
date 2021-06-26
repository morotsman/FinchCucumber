package steps

import io.cucumber.scala.{EN, ScalaDsl}
import io.finch.Input
import org.scalatestplus.scalacheck.Checkers.check
import org.scalacheck.Arbitrary
import org.scalatestplus.scalacheck.Checkers._
import steps.helpers.PrerequisiteException

class GetMachinesSteps extends ScalaDsl with EN {

  When("""checking the statuses of the candy machines in the park""") { () =>
    World.context = World.context.copy(getMachinesRequest = Some(Input.get("/machine")))
  }

  Then("""the status of the candy machines should be returned, sorted by id""") { () =>
    implicit val app: Arbitrary[TestApp] =
      World.context.appGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine park generator"))
    val request =
      World.context.getMachinesRequest.getOrElse(throw new PrerequisiteException("Expecting a finch action"))

    check { (app: TestApp) =>
      val shouldBeTrue = for {
        prev <- app.state
        machines <- app.getMachines(request).output.get
        next <- app.state
      } yield
        stateUnChanged(prev, next) && machines.value == prev.store.values.toList.sortBy(_.id)

      shouldBeTrue.unsafeRunSync()
    }

  }

  private def stateUnChanged(prev: AppState, next: AppState): Boolean =
    sameId(prev, next) && storeSame(prev, next)

  private def sameId(prev: AppState, next: AppState): Boolean =
    prev.id == next.id

  private def storeSame(prev: AppState, next: AppState): Boolean =
    prev.store == next.store
}