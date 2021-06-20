package steps

import cats._
import cats.instances._
import cats.effect.IO
import cats.implicits.{catsStdInstancesForOption, catsSyntaxApplicativeId, toTraverseOps}
import com.github.morotsman.investigate_finagle_service.candy_finch.{Coin, MachineInput, MachineState, Turn}
import com.twitter.finagle.http.Status
import io.cucumber.scala.{EN, ScalaDsl}
import io.finch.{Input, Output}
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.Checkers.check
import steps.World.spec
import steps.helpers.PrerequisiteException

class MachineInputSteps extends ScalaDsl with EN {

  When("""the customer inserts a coin in a candy machine that has not been added to the park""") { () =>
    spec.add(context => {
      context.copy(insertCoinRequest2 =
        Some((machine, testApp) => appState => {
          val unknownId = appState.id + 1
          val input = Input.put(s"/machine/$unknownId/coin")
          val output: Option[IO[Output[MachineState]]] = testApp.insertCoin(input).output
          output.map(_.map(v => (machine.withId(unknownId), v)))
        }))
    })
  }

  Then("""the coin should be rejected""") { () =>
    spec.validate(context => {
      implicit val machine: Arbitrary[Option[MachineWithoutId]] = sequence(context.machineGenerator)
      implicit val app: Arbitrary[Option[TestApp]] = sequence(context.appGenerator)
      val action = context.insertCoinRequest2

      check { (app: Option[TestApp], randomMachine: Option[MachineWithoutId]) =>
        val shouldBeTrue = for {
          myApp <- app
          myMachine <- randomMachine
          myAction <- action
        } yield test(myApp, myAction(myMachine, myApp))

        (for {
          iob <- shouldBeTrue
          ob <- iob.unsafeRunSync()
        } yield ob).getOrElse(throw new RuntimeException("Something is missing in the setup"))
      }
    })

    spec.value().unsafeRunSync()
  }

  private def sequence[A](oa: Option[Arbitrary[A]]): Arbitrary[Option[A]] = oa match {
    case Some(aa) => Arbitrary(aa.arbitrary.map(Some(_)))
    case None => Arbitrary(Gen.oneOf(None, None))
  }

  private def test(
                    testApp: TestApp,
                    output: AppState => Option[IO[(MachineState, Output[MachineState])]]): IO[Option[Boolean]] = for {
    prevAppState <- testApp.state
    machineAndOutput <- output(prevAppState).sequence
    nextAppState <- testApp.state
  } yield machineAndOutput.map(mo => mo._2.status match {
    case Status.NotFound =>
      stateUnChanged(prevAppState, nextAppState) && machineUnknown(mo._1.id, prevAppState)
    case Status.BadRequest =>
      stateUnChanged(prevAppState, nextAppState) && machineInWrongState(mo._1.id, prevAppState, Coin)
    case _ =>
      false
  })


  private def machineUnknown(id: Int, prev: AppState): Boolean =
    !prev.store.contains(id)

  private def stateUnChanged(prev: AppState, next: AppState): Boolean = {
    val unchanged = sameId(prev, next) && storeSame(prev, next)
    println("stateUnChanged: " + unchanged)
    unchanged
  }

  private def sameId(prev: AppState, next: AppState): Boolean = {
    val sameId = prev.id == next.id
    println("sameId: " + sameId)
    sameId
  }

  private def storeSame(prev: AppState, next: AppState): Boolean =
    prev.store == next.store

  def machineInWrongState(id: Int, prev: AppState, command: MachineInput): Boolean = command match {
    case Turn =>
      println("kommer hit 1")
      prev.store(id).locked || prev.store(id).candies <= 0
    case Coin =>
      println("kommer hit 2")
      val state = !prev.store(id).locked || prev.store(id).candies <= 0
      println("state: " + state)
      state
  }

  When("""a coin is inserted in a locked candy machine""") {
    () =>
      // Write code here that turns the phrase above into concrete actions
      throw new io.cucumber.scala.PendingException()
  }

  Then("""the candy machine should be unlocked""") {
    () =>
      // Write code here that turns the phrase above into concrete actions
      throw new io.cucumber.scala.PendingException()
  }

  When("""a coin is inserted in a unlocked candy machine""") {
    spec.add(context => {
      context.copy(insertCoinRequest2 =
        Some((machine, testApp) => appState => {


          appState.store.find(_._2.locked).flatMap(im => {
            val input = Input.put(s"/machine/${im._1}/coin")
            val output: Option[IO[Output[MachineState]]] = testApp.insertCoin(input).output
            output.map(_.map(v => (im._2, v)))
          })
        }))
    })
  }
}