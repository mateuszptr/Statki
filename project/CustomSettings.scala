import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object CustomSettings {
  val name = "Statki"
  val version = "0.0.1"

  object versions {
    val scala = "2.12.5"
    val scalaDom = "0.9.3"
    val circe = "0.9.3"
    val akka = "2.5.12"
    val akkaHttp = "10.1.1"
    val scalatest = "3.0.1"
    val paradise = "2.1.1"
    val akkaJs = "1.2.5.12"
  }

  val sharedDependencies = Def.setting(Seq(
    "io.circe" %%% "circe-core" % versions.circe,
    "io.circe" %%% "circe-generic" % versions.circe,
    "io.circe" %%% "circe-parser" % versions.circe

  ))

  val jvmDependencies = Def.setting(Seq(
    "com.typesafe.akka" %% "akka-http" % versions.akkaHttp,
    "com.typesafe.akka" %% "akka-http-spray-json" % versions.akkaHttp,
    "com.typesafe.akka" %% "akka-http-xml" % versions.akkaHttp,
    "com.typesafe.akka" %% "akka-stream" % versions.akka,

    "com.typesafe.akka" %% "akka-http-testkit" % versions.akkaHttp % Test,
    "com.typesafe.akka" %% "akka-testkit" % versions.akka % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % versions.akka % Test,
    "org.scalatest" %% "scalatest" % versions.scalatest % Test
  ))

  val scalajsDependencies = Def.setting(Seq(
    "org.scala-js" %%% "scalajs-dom" % versions.scalaDom,
    "org.akka-js" %%% "akkajsactorstream" % versions.akkaJs,
    "org.akka-js" %%% "akkajsactor" % versions.akkaJs
  ))

  val jsDependencies = Def.setting(Seq(

  ))
}
