package steps.helpers

import shapeless.{HList, HNil}

object ShapelessContext extends App {

  val tmp: HNil = HNil

  val tmp2 = "1" :: HNil

  val tmp3 = "Hello" :: 123 :: true :: HNil

  val tmp4 = tmp2 ++ tmp3


  println(tmp4.head)
  println(tmp4.tail.head)
  println(tmp4.tail.tail.head)
  println(tmp4.tail.tail.tail.head)
}
