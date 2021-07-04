package steps

import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.finch.Output
import org.scalacheck.Arbitrary
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.Checkers.check
import steps.helpers.PrerequisiteException

object Validator {

  def validate[A](action: Action[A])(validator: (AppState, (A, Output[A]), AppState) => Boolean): Assertion = {
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

  def machineUnknown(id: Int, prev: AppState): Boolean =
    !prev.store.contains(id)

  def stateUnChanged(prev: AppState, next: AppState): Boolean =
    sameId(prev, next) && storeSame(prev, next)

  def sameId(prev: AppState, next: AppState): Boolean =
    prev.id == next.id

  def storeSame(prev: AppState, next: AppState): Boolean =
    prev.store == next.store

}
