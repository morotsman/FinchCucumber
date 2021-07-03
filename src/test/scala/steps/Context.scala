package steps

import cats.effect.IO
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.finch.Output
import org.scalacheck.Arbitrary

case class Action[A](run: (MachineWithoutId, TestApp) => IO[Option[(A, Output[A])]])

case class Context(
                    appGenerator: Option[Arbitrary[TestApp]],
                    machineGenerator: Option[Arbitrary[MachineWithoutId]],
                    finchAction: Option[Action[MachineState]],
                    getMachinesRequest: Option[Action[List[MachineState]]],
  )

object Context {
  def emptyContext: Context = Context(None, None, None, None)
}
