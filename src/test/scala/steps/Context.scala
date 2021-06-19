package steps

import io.finch.Application.Json
import io.finch.Input
import org.scalacheck.Arbitrary

case class Context(
                    appGenerator: Option[Arbitrary[TestApp]],
                    machineGenerator: Option[Arbitrary[MachineWithoutId]],
                    createMachineRequest: Option[Input.Body[Json]],
                    getMachinesRequest: Option[Input],
  )

object Context {
  def emptyContext: Context = Context(None, None, None, None)
}
