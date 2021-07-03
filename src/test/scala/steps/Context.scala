package steps

import cats.effect.IO
import io.finch.Output
import org.scalacheck.Arbitrary

case class Action[A](run: (MachineWithoutId, TestApp) => IO[Option[(A, Output[A])]])

case class Context(
                    appGenerator: Option[Arbitrary[TestApp]] = None,
                    machineGenerator: Option[Arbitrary[MachineWithoutId]] = None
  )
