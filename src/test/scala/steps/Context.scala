package steps

import io.finch.Application.Json
import io.finch.Input
import org.scalacheck.{Arbitrary, Gen}

case class Context(
                    number: Option[Int],
                    appGenerator: Option[Arbitrary[TestApp]],
                    machineGenerator: Option[Arbitrary[MachineWithoutId]],
                    createMachineRequest: Option[Input.Body[Json]],
                    getMachinesRequest: Option[Input]
                  )

object Context {
  def emptyContext: Context = Context(None, None, None, None, None)
}
