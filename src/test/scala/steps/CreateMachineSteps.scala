package steps

import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.cucumber.scala.{EN, ScalaDsl}
import io.finch.{Application, Input, Output}
import org.scalatestplus.scalacheck.Checkers.check
import cats.effect.IO
import io.circe.generic.auto._
import io.finch.circe._
import org.scalacheck.Arbitrary
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.Checkers._
import steps.helpers.PrerequisiteException

class CreateMachineSteps extends ScalaDsl with EN {

  When("""the candy machine is added to the park""") { () =>
    World.context = World.context.copy(createMachineRequest = Some(Input.post("/machine").withBody[Application.Json]))
  }

  Then("""the machine should be allocated an unique id""") { () =>
    validateMachineCreation(World.context) { (_, prevAppState, addedMachine, nextAppState) =>
      prevAppState.id + 1 == nextAppState.id && !prevAppState.store.contains(addedMachine.value.id)
    }
  }

  Then("""the machine should be added to the park""") { () =>
    validateMachineCreation(World.context) { (machineToAdd, prevAppState, addedMachine, nextAppState) =>
      prevAppState.store + (prevAppState.id -> addedMachine.value) == nextAppState.store &&
        addedMachine.value == machineToAdd.withId(prevAppState.id)
    }
  }

  def validateMachineCreation(context: Context)(validator: (MachineWithoutId, AppState, Output[MachineState], AppState) => Boolean): Assertion = {
    implicit val machine: Arbitrary[MachineWithoutId] =
      context.machineGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine generator"))
    implicit val app: Arbitrary[TestApp] =
      context.appGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine park generator"))
    val request = context.createMachineRequest.getOrElse(throw new PrerequisiteException("Expecting a finch action"))

    check { (app: TestApp, machineToAdd: MachineWithoutId) =>
      val shouldBeTrue: IO[Boolean] = for {
        appStateBeforeOperation <- app.state
        addedMachine <- app.createMachine(request(machineToAdd)).output.get
        appStateAfterOperation <- app.state
      } yield validator(machineToAdd, appStateBeforeOperation, addedMachine, appStateAfterOperation)

      shouldBeTrue.unsafeRunSync()
    }
  }
}