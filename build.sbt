name := "ScalaPT"

version := "1.0"

scalaVersion := "2.11.8"

val circeVersion = "0.5.1"

//libraryDependencies += "org.scala-lang" %% "scala-reflect" % "2.11.8"

libraryDependencies += "org.typelevel" %% "cats" % "0.5.0"

libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "org.typelevel" %% "cats" % "0.7.2"


mainClass in Compile := Some("scalapt.MainFrame")
