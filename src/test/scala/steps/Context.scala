package steps

import io.finch
import io.finch.Application.Json
import io.finch.Input
import org.scalacheck.Gen

case class Context(
                    number: Option[Int],
                    appGenerator: Option[Gen[TestApp]],
                    machineGenerator: Option[Gen[MachineWithoutId]],
                    createMachineRequest: Option[Input.Body[Json]],
                    getMachinesRequest: Option[Input]
                  )

object Context {
  def emptyContext: Context = Context(None, None, None, None, None)
}
