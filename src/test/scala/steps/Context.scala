package steps

import cats.effect.IO
import com.github.morotsman.investigate_finagle_service.candy_finch.MachineState
import io.finch.Output
import org.scalacheck.Arbitrary

case class Action[A](run: (MachineWithoutId, TestApp) => IO[Option[(A, Output[A])]])

case class Context(
                    appGenerator: Option[Arbitrary[TestApp]] = None,
                    machineGenerator: Option[Arbitrary[MachineWithoutId]] = None,
                    finchAction: Option[Action[MachineState]] = None,
                    finchListAction: Option[Action[List[MachineState]]] = None,
  )
