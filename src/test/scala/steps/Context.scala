package steps

import cats.effect.IO




object Context {

  def apply[A](a: A): Context[A] = new Context[A](a)

}

class Context[A](a: A) {

  var testSpec: IO[A] = IO.pure(a)

  def map(f: A => A): Unit = {
    testSpec = testSpec.flatMap(a => IO {
      f(a)
    })
  }

  def foreach(f: A => Unit): Unit = {
    testSpec = testSpec.flatMap(a => IO {
      f(a)
      a
    })
  }

  def run(): Unit = testSpec.unsafeRunSync()

}

