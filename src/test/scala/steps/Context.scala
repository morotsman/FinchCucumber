package steps

import cats.effect.IO
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.finch.Output
import org.scalacheck.Arbitrary

case class Context(
                    appGenerator: Option[Arbitrary[TestApp]],
                    machineGenerator: Option[Arbitrary[MachineWithoutId]],
                    createMachineRequest: Option[(MachineWithoutId, TestApp) => IO[Option[(MachineState, Output[MachineState])]]],
                    getMachinesRequest: Option[TestApp => IO[Option[Output[List[MachineState]]]]],
                    machineInputRequest: Option[(MachineWithoutId, TestApp) => IO[Option[(MachineState, Output[MachineState])]]],
  )

object Context {
  def emptyContext: Context = Context(None, None, None, None, None)
}
