lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example.interview",
      scalaVersion := "2.13.5"
    )),
    name := "demo",
    version := "1.0-SNAPSHOT"
  )

mainClass in (Compile, run) := Some("com.example.interview.Main")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.5" % Test,
  "com.lihaoyi" %% "upickle" % "1.2.3",
  "com.squareup.okhttp3" % "okhttp" % "4.9.0"
)
