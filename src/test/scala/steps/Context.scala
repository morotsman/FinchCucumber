package steps

import cats.effect.IO
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.finch.Output
import org.scalacheck.Arbitrary
import steps.helpers.PrerequisiteException

case class Action[A](run: (MachineWithoutId, TestApp) => IO[Option[(A, Output[A])]])

case class Context(
                    appGenerator: Option[Arbitrary[TestApp]],
                    machineGenerator: Option[Arbitrary[MachineWithoutId]],
                    finchAction: Option[Action[MachineState]],
                    getMachinesRequest: Option[Action[List[MachineState]]],
  )

object Context {
  def emptyContext: Context = Context(None, None, None, None)

  def withContext[A](fun: (Arbitrary[MachineWithoutId], Arbitrary[TestApp], Action[MachineState]) => A): A = {
    implicit val machine: Arbitrary[MachineWithoutId] =
      World.context.machineGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine generator"))
    implicit val app: Arbitrary[TestApp] =
      World.context.appGenerator.getOrElse(throw new PrerequisiteException("Expecting a machine park generator"))
    val request: Action[MachineState] = World.context.finchAction.getOrElse(throw new PrerequisiteException("Expecting a finch action"))
    fun(machine, app, request)
  }

}
