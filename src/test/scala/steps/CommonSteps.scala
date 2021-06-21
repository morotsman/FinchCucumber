package steps

import com.github.morotsman.investigate_finagle_service.candy_finch.{App, MachineState}
import io.cucumber.scala.{EN, ScalaDsl}
import steps.World.spec
import cats.effect.IO
import cats.effect.concurrent.Ref
import io.finch.internal.DummyExecutionContext
import org.scalacheck.{Arbitrary, Gen}

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

class CommonSteps extends ScalaDsl with EN {
  private def genMachineWithoutId(unlocked: Boolean = false, locked: Boolean = false): Gen[MachineWithoutId] = for {
    locked <- if (unlocked)
      Gen.oneOf(false, false)
    else if(locked)
      Gen.oneOf(true, true)
    else
      Gen.oneOf(true, false)
    candies <- Gen.choose(0, 3)
    coins <- Gen.choose(0, 1000)
  } yield MachineWithoutId(locked, candies, coins)

  private def genTestApp: Gen[TestApp] =
    Gen.listOf(genMachineWithoutId()).map { machines =>
      val id = machines.length
      val store = machines.zipWithIndex.map { case (m, i) => i -> m.withId(i) }

      TestApp(Ref.unsafe[IO, Int](id), Ref.unsafe[IO, Map[Int, MachineState]](store.toMap))
    }

  Given("""a park of candy machines""") { () =>
    spec.add(context => {
      context.copy(appGenerator = Some(Arbitrary(genTestApp)))
    })
  }

  Given("""a candy machine""") { () =>
    spec.add(context => {
      context.copy(machineGenerator = Some(Arbitrary(genMachineWithoutId())))
    })
  }

  Given("""an unlocked machine""") { () =>
    spec.add(context => {
      context.copy(machineGenerator = Some(Arbitrary(genMachineWithoutId(true))))
    })
  }

  Given("""an locked machine""") { () =>
    spec.add(context => {
      context.copy(machineGenerator = Some(Arbitrary(genMachineWithoutId(true))))
    })
  }
}
