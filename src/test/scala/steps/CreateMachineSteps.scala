package steps

import com.github.morotsman.investigate_finagle_service.candy_finch.{App, MachineState}
import io.cucumber.scala.{EN, ScalaDsl}
import io.finch.{Application, Input}
import org.scalatestplus.scalacheck.Checkers.check
import steps.World.spec
import cats.effect.IO
import cats.effect.concurrent.Ref
import io.circe.generic.auto._
import io.finch
import io.finch.circe._
import io.finch.internal.DummyExecutionContext
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.Checkers._
import steps.helpers.PrerequisiteException

case class AppState(id: Int, store: Map[Int, MachineState])

case class TestApp(
                    id: Ref[IO, Int],
                    store: Ref[IO, Map[Int, MachineState]]
                  ) extends App(id, store)(IO.contextShift(DummyExecutionContext)) {
  def state: IO[AppState] = for {
    i <- id.get
    s <- store.get
  } yield AppState(i, s)
}

case class MachineWithoutId(locked: Boolean, candies: Int, coins: Int) {
  def withId(id: Int): MachineState = MachineState(id, locked, candies, coins)
}

class CreateMachineSteps extends ScalaDsl with EN {

  private val genMachineWithoutId: Gen[MachineWithoutId] = for {
    locked <- Gen.oneOf(true, false)
    candies <- Gen.choose(0, 3)
    coins <- Gen.choose(0, 1000)
  } yield MachineWithoutId(locked, candies, coins)

  private def genTestApp: Gen[TestApp] =
    Gen.listOf(genMachineWithoutId).map { machines =>
      val id = machines.length
      val store = machines.zipWithIndex.map { case (m, i) => i -> m.withId(i) }

      TestApp(Ref.unsafe[IO, Int](id), Ref.unsafe[IO, Map[Int, MachineState]](store.toMap))
    }

  Given("""a park of candy machines""") { () =>
    spec + (context => {
      context.copy(appGenerator = Some(genTestApp), machineGenerator = Some(genMachineWithoutId))
    })
  }

  When("""another candy machine is added""") { () =>
    spec + (context => {
      context.copy(createMachineRequest = Some(Input.post("/machine").withBody[Application.Json]))
    })
  }

  Then("""the machine should be allocated an unique id""") { () =>
    spec.validate(context => {
      implicit val machine = Arbitrary(
        context.machineGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine generator"))
      )
      implicit val app = Arbitrary(
        context.appGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine park generator"))
      )
      val request = context.createMachineRequest.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
      check { (app: TestApp, machine: MachineWithoutId) =>
        val input: finch.Input = request(machine)
        val shouldBeTrue: IO[Boolean] = for {
          prev <- app.state
          newMachine <- app.createMachine(input).output.get
          next <- app.state
        } yield
          prev.id + 1 == next.id &&
          !prev.store.contains(newMachine.value.id)

        shouldBeTrue.unsafeRunSync()
      }
    })
    spec.run().unsafeRunSync()
  }

  Then("""the machine should be added to the park""") { () =>
    spec.validate(context => {
      implicit val machine = Arbitrary(
        context.machineGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine generator"))
      )
      implicit val app = Arbitrary(
        context.appGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine park generator"))
      )
      val request = context.createMachineRequest.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
      check { (app: TestApp, machine: MachineWithoutId) =>
        val input: finch.Input = request(machine)
        val shouldBeTrue: IO[Boolean] = for {
          prev <- app.state
          newMachine <- app.createMachine(input).output.get
          next <- app.state
        } yield
          prev.store + (prev.id -> newMachine.value) == next.store &&
          newMachine.value == machine.withId(prev.id)

        shouldBeTrue.unsafeRunSync()
      }
    })
    spec.run().unsafeRunSync()
  }
}