package steps

import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.finch.Output
import org.scalacheck.Arbitrary
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.Checkers.check
import steps.helpers.PrerequisiteException

object Validator {
  def validateAction(validator: (AppState, (MachineState, Output[MachineState]), AppState) => Boolean): Assertion = {
    implicit val machine: Arbitrary[MachineWithoutId] =
      World.context.machineGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine generator"))
    implicit val app: Arbitrary[TestApp] =
      World.context.appGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine park generator"))
    val request: Action[MachineState] = World.context.finchAction.getOrElse(throw new PrerequisiteException("Expecting a finch action"))

    check { (app: TestApp, machineToAdd: MachineWithoutId) =>
      val shouldBeTrue = for {
        appStateBeforeOperation <- app.state
        output <- request.run(machineToAdd, app)
        appStateAfterOperation <- app.state
      } yield output.map(o =>
        validator(appStateBeforeOperation, o, appStateAfterOperation)
      )

      shouldBeTrue.unsafeRunSync()
        .getOrElse(throw new PrerequisiteException("Could not execute the finch action"))
    }
  }
}
