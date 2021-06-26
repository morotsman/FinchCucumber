name := "investigae_thrift_finagle"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  // needed to create http service
  "com.twitter" %% "finagle-http" % "20.12.0",

  // needed for scrooge https://twitter.github.io/scrooge/SBTPlugin.html
  "org.apache.thrift" % "libthrift" % "0.10.0",
  "com.twitter" %% "scrooge-core" % "20.12.0",
  "com.twitter" %% "finagle-thrift" % "20.12.0",

  // convert to and from stuff (for example twitter future to scala future)
  "com.twitter" %% "bijection-core" % "0.9.7",
  "com.twitter" %% "bijection-util" % "0.9.7",
  "com.twitter" %% "bijection-json" % "0.9.7",


  // finch
  "com.github.finagle" %% "finchx-core" % "0.32.0",
  "com.github.finagle" %% "finchx-circe" % "0.32.0",
  "io.circe" %% "circe-generic" % "0.13.0",

  // cats
  "org.typelevel" %% "cats-core" % "2.1.1",
  "org.typelevel" %% "cats-effect" % "2.1.1",

  "com.chuusai" %% "shapeless" % "2.3.3",

  // Twitter server
  "com.twitter" %% "twitter-server" % "20.12.0",

  // Test
  "org.scalatest" %% "scalatest" % "3.2.3" % "test",
  "org.scalacheck" %% "scalacheck" % "1.15.2" % "test",
  "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % "test"
)

val cucumberVersion = "6.10.4"
val junitVersion = "4.13.2"

libraryDependencies += "io.cucumber" %% "cucumber-scala" % "7.0.0" % Test
libraryDependencies += "io.cucumber" % "cucumber-junit" % cucumberVersion % Test

libraryDependencies += "junit" % "junit" % junitVersion % Test
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test

