import sbt.addCompilerPlugin

lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion = "2.5.12"

lazy val circeVersion = "0.9.3"




//lazy val root = (project in file(".")).
//  settings(
//    inThisBuild(List(
//      organization := "xyz.statki",
//      scalaVersion := "2.12.5"
//    )),
//    name := "Statki",
//    libraryDependencies ++= Seq(
//      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
//      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
//      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
//      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
//
//      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
//      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
//      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
//      "org.scalatest" %% "scalatest" % "3.0.1" % Test
//    ),
//    libraryDependencies ++= Seq(
//      "io.circe" %% "circe-core",
//      "io.circe" %% "circe-generic",
//      "io.circe" %% "circe-parser"
//    ).map(_ % circeVersion)
//  )

//addCompilerPlugin(
//  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
//)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(
    scalaVersion := CustomSettings.versions.scala,
    libraryDependencies ++= CustomSettings.sharedDependencies.value,
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
  )

lazy val sharedJVM = shared.jvm.settings(name := "sharedJVM")
lazy val sharedJS = shared.js.settings(name := "sharedJS")

lazy val client: Project = (project in file("client"))
  .settings(
    name := "client",
    version := CustomSettings.version,
    scalaVersion := CustomSettings.versions.scala,
    libraryDependencies ++= CustomSettings.scalajsDependencies.value,
    jsDependencies ++= CustomSettings.jsDependencies.value,
    jsDependencies += RuntimeDOM % "test",
    skip in packageJSDependencies := false,
    scalaJSUseMainModuleInitializer := true,
    scalaJSUseMainModuleInitializer in Test := false,
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(sharedJS)

lazy val clients = Seq(client)

lazy val server: Project = (project in file("server"))
  .settings(
    name := "server",
    version := CustomSettings.version,
    scalaVersion := CustomSettings.versions.scala,
    libraryDependencies ++= CustomSettings.jvmDependencies.value,
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
  )
  .dependsOn(sharedJVM)