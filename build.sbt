name := "ScalaPT"

version := "1.0"

scalaVersion := "2.11.8"

val circeVersion = "0.4.1"

libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
).map(_ % circeVersion)

//mainClass in Compile := Some("scalapt.MainFrame")
