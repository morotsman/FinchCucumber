package steps

import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.finch.Output
import org.scalacheck.Arbitrary
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.Checkers.check
import steps.helpers.PrerequisiteException

object Validator {
  def validateAction(validator: (AppState, (MachineState, Output[MachineState]), AppState) => Boolean): Assertion = {
    val action = World.context.finchAction.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
    genericValidator(action, validator)
  }

  def validateListAction(validator: (AppState, (List[MachineState], Output[List[MachineState]]), AppState) => Boolean): Assertion = {
    val action = World.context.finchListAction.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
    genericValidator(action, validator)
  }

  private def genericValidator[A](
                                   action: Action[A],
                                   validator: (AppState, (A, Output[A]), AppState) => Boolean,
                                 ): Assertion = {
    implicit val machine: Arbitrary[MachineWithoutId] =
      World.context.machineGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine generator"))
    implicit val app: Arbitrary[TestApp] =
      World.context.appGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine park generator"))

    check { (machineToAdd: MachineWithoutId, app: TestApp) =>
      val shouldBeTrue = for {
        prev <- app.state
        machines <- action.run(machineToAdd, app)
        next <- app.state
      } yield machines.map(ms =>
        validator(prev, ms, next)
      )

      shouldBeTrue.unsafeRunSync()
        .getOrElse(throw new PrerequisiteException("Could not execute the finch action"))
    }
  }

}
