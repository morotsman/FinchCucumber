package steps

import cats.data.OptionT
import cats.effect.IO
import cats.implicits.{catsStdInstancesForOption, toTraverseOps}
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.cucumber.scala.{EN, ScalaDsl}
import io.finch.{Application, Input, Output}
import org.scalatestplus.scalacheck.Checkers.check
import io.circe.generic.auto._
import io.finch.circe._
import org.scalacheck.Arbitrary
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.Checkers._
import steps.helpers.PrerequisiteException

class CreateMachineSteps extends ScalaDsl with EN {

  When("""the candy machine is added to the park""") { () =>
    World.context = World.context.copy(createMachineRequest = Some((machine, app) => {
      val createMachineRequest = Input.post("/machine").withBody[Application.Json]
      val result = OptionT(app.createMachine(createMachineRequest(machine)).output.sequence)
      result.map(r => (r.value, r)).value
    }))
  }

  Then("""the machine should be allocated an unique id""") { () =>
    validateMachineCreation(World.context) { (prevAppState, mo, nextAppState) =>
      prevAppState.id + 1 == nextAppState.id && !prevAppState.store.contains(mo._2.value.id)
    }
  }

  Then("""the machine should be added to the park""") { () =>
    validateMachineCreation(World.context) { (prevAppState, mo, nextAppState) =>
      prevAppState.store + (prevAppState.id -> mo._2.value) == nextAppState.store
    }
  }

  def validateMachineCreation(context: Context)(validator: (AppState, (MachineState, Output[MachineState]), AppState) => Boolean): Assertion = {
    implicit val machine: Arbitrary[MachineWithoutId] =
      context.machineGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine generator"))
    implicit val app: Arbitrary[TestApp] =
      context.appGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine park generator"))
    val request = context.createMachineRequest.getOrElse(throw new PrerequisiteException("Expecting a finch action"))

    check { (app: TestApp, machineToAdd: MachineWithoutId) =>
      val shouldBeTrue = for {
        appStateBeforeOperation <- app.state
        output <- request(machineToAdd, app)
        appStateAfterOperation <- app.state
      } yield output.map(o =>
        validator(appStateBeforeOperation, o, appStateAfterOperation)
      )

      shouldBeTrue.unsafeRunSync()
        .getOrElse(throw new PrerequisiteException("Could not execute the finch action"))
    }
  }
}