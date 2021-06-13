package steps

import cats.Monad
import cats.implicits._

import scala.language.higherKinds

object Spec {
  def apply[F[_] : Monad, A](a: A): Spec[F, A] = new Spec[F, A](a)
}

class Spec[F[_] : Monad, A](a: A) {
  private var spec: F[A] = a.pure[F]

  private def add(spec: F[A]): Unit = this.spec = spec

  def run(): F[A] = spec

  def add(f: A => A): Unit = add(spec.flatMap(a =>
    f(a).pure[F]
  ))

  def +(f: A => A): Unit = add(f)

  def validate(f: A => Unit): Unit = add(spec.flatMap(a => {
    f(a)
    a.pure[F]
  }))
}



