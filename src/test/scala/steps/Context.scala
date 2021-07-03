package steps

import cats.effect.IO
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.finch.Output
import org.scalacheck.Arbitrary

case class Action(run: (MachineWithoutId, TestApp) => IO[Option[(MachineState, Output[MachineState])]])

case class Context(
                    appGenerator: Option[Arbitrary[TestApp]],
                    machineGenerator: Option[Arbitrary[MachineWithoutId]],
                    createMachineRequest: Option[Action],
                    getMachinesRequest: Option[(MachineWithoutId, TestApp) => IO[Option[(List[MachineState], Output[List[MachineState]])]]],
                    machineInputRequest: Option[Action],
  )

object Context {
  def emptyContext: Context = Context(None, None, None, None, None)
}
