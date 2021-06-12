package steps

import cats.effect.IO
import io.cucumber.scala.{ScalaDsl, Scenario}

object Context {

  private var testSpec: IO[Unit] = IO.pure()

  def fGiven(f: () => Unit): Unit = {
    Context.testSpec = Context.testSpec.flatMap(_ => IO {
      f()
    })
  }

  def fThen(f: () => Unit): Unit = {
    Context.testSpec = Context.testSpec.flatMap(_ => IO {
      f()
    })
  }

}

class Context extends ScalaDsl {

  After { scenario: Scenario =>
    Context.testSpec.unsafeRunSync()
  }

}

