package steps.helpers

import cats.Monad
import cats.implicits._

import scala.language.higherKinds

object Spec {
  def apply[F[_] : Monad, A](a: A): Spec[F, A] = new Spec[F, A](a)
}

class Spec[F[_] : Monad, A](a: A) {
  private var spec: F[A] = a.pure[F]

  private def updateSpec(spec: F[A]): Unit = this.spec = spec

  def add(f: A => A): Unit = updateSpec(spec.map(f))

  def validate(f: A => Unit): Unit = {
    updateSpec(spec.map(a => {
      f(a)
      a
    }))
  }

  def value(): F[A] = spec
}



