package steps

import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.finch.Output
import org.scalacheck.Arbitrary
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.Checkers.check
import steps.helpers.PrerequisiteException
import steps.Context._

object Validator {
  def validateAction(validator: (AppState, (MachineState, Output[MachineState]), AppState) => Boolean): Assertion = {
    withContext { (machine, app, request) =>
      implicit val implicitApp: Arbitrary[TestApp] = app
      implicit val implicitMachine: Arbitrary[MachineWithoutId] = machine

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
}
