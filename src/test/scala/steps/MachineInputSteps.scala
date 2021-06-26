package steps

import cats.data.OptionT
import cats.implicits.{catsStdInstancesForOption, toTraverseOps}
import com.github.morotsman.investigate_finagle_service.candy_finch.{Coin, MachineInput, Turn}
import com.twitter.finagle.http.Status
import org.scalacheck.Gen
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

class MachineInputSteps extends ScalaDsl with EN {

  When("""the customer inserts a coin in a candy machine that has not been added to the park""") { () =>
    World.context = World.context.copy(request =
        Some((machine, testApp) => appState => {
          val unknownId = appState.id + 1
          val insertCoinRequest = Input.put(s"/machine/$unknownId/coin")
          val result = OptionT(testApp.insertCoin(insertCoinRequest).output.sequence)
          result.map(om => (machine.withId(unknownId), om)).value
        }))
  }

  When("""a coin is inserted in the candy machine""") {
    World.context = World.context.copy(request =
      Some(value = (machine, testApp) => _ => {
        val createMachineRequest = Input.post("/machine").withBody[Application.Json]
        val createMachine = OptionT(testApp.createMachine(createMachineRequest(machine)).output.sequence)

        val insertCoinRequest = (id: Int) => Input.put(s"/machine/$id/coin")
        val insertCoinInMachine = (id: Int) => OptionT(testApp.insertCoin(insertCoinRequest(id)).output.sequence)

        (for {
          machine <- createMachine
          result <- insertCoinInMachine(machine.value.id)
        } yield (machine.value, result)).value
      }))
  }


  Then("""the coin should be rejected""") { () =>
    validate(World.context)
  }

  Then("""the candy machine should be unlocked""") { () =>
    validate(World.context)
  }

  def validate(context: Context): Assertion = {
    implicit val machine: Arbitrary[Option[MachineWithoutId]] = sequence(context.machineGenerator)
    implicit val app: Arbitrary[Option[TestApp]] = sequence(context.appGenerator)
    val request = context.request

    check { (app: Option[TestApp], randomMachine: Option[MachineWithoutId]) =>
      val shouldBeTrue = for {
        myApp <- app
        myMachine <- randomMachine
        myRequest <- request
      } yield test(myApp, myRequest(myMachine, myApp))

      (for {
        iob <- shouldBeTrue
        ob <- iob.unsafeRunSync()
      } yield ob).getOrElse(throw new RuntimeException("Something is missing in the setup"))
    }
  }

  private def test(
                    testApp: TestApp,
                    request: AppState => IO[Option[(MachineState, Output[MachineState])]]): IO[Option[Boolean]] = {
    for {
      prevAppState <- testApp.state
      machineAndOutput <- request(prevAppState)
      nextAppState <- testApp.state
    } yield machineAndOutput.map(mo => mo._2.status match {
      case Status.NotFound =>
        stateUnChanged(prevAppState, nextAppState) && machineUnknown(mo._1.id, prevAppState)
      case Status.BadRequest =>
        stateUnChanged(addMachineToState(mo._1, prevAppState), nextAppState) && machineInWrongState(mo._1.id, nextAppState, Coin)
      case Status.Ok =>
        isUnlocked(mo._1.id, addMachineToState(mo._1, prevAppState), nextAppState)
      case _ =>
        false
    })
  }

  def addMachineToState(m: MachineState, app: AppState): AppState =
    app.copy(
      id = app.id + 1,
      store = app.store + (m.id -> m)
    )


  private def sequence[A](oa: Option[Arbitrary[A]]): Arbitrary[Option[A]] = oa match {
    case Some(aa) => Arbitrary(aa.arbitrary.map(Some(_)))
    case None => Arbitrary(Gen.oneOf(None, None))
  }

  private def machineUnknown(id: Int, prev: AppState): Boolean =
    !prev.store.contains(id)

  private def stateUnChanged(prev: AppState, next: AppState): Boolean =
    sameId(prev, next) && storeSame(prev, next)

  private def sameId(prev: AppState, next: AppState): Boolean =
    prev.id == next.id

  private def storeSame(prev: AppState, next: AppState): Boolean =
    prev.store == next.store

  def machineInWrongState(id: Int, prev: AppState, command: MachineInput): Boolean = command match {
    case Turn =>
      prev.store(id).locked || prev.store(id).candies <= 0
    case Coin =>
      !prev.store(id).locked || prev.store(id).candies <= 0
  }

  def isUnlocked(id: Int, prevState: AppState, nextState: AppState): Boolean = (for {
    prev <- prevState.store.get(id)
    next <- nextState.store.get(id)
    if prev.locked && !next.locked
    if prev.candies > 0 && next.candies == prev.candies && next.coins == prev.coins + 1
    if sameId(prevState, nextState)
    if prevState.store.filter(kv => kv._1 != id) == nextState.store.filter(kv => kv._1 != id)
  } yield true).getOrElse(false)

}