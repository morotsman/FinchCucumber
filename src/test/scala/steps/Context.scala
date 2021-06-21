package steps

import cats.effect.IO
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.finch.Application.Json
import io.finch.{Input, Output}
import org.scalacheck.Arbitrary

case class Context(
                    appGenerator: Option[Arbitrary[TestApp]],
                    machineGenerator: Option[Arbitrary[MachineWithoutId]],
                    createMachineRequest: Option[Input.Body[Json]],
                    getMachinesRequest: Option[Input],
                    insertCoinRequest: Option[(MachineWithoutId, TestApp) => AppState => IO[Option[(MachineState, Output[MachineState])]]],
  )

object Context {
  def emptyContext: Context = Context(None, None, None, None, None)
}
