name := "ScalaPT"

version := "1.0"

scalaVersion := "2.11.8"

val circeVersion = "0.5.4"
val catsVersion = "0.7.2"
val scalaLogVersion = "3.5.0"
val log4jVersion = "2.7"

//libraryDependencies += "org.scala-lang" %% "scala-reflect" % "2.11.8"

libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "org.typelevel" %% "cats" % catsVersion

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % scalaLogVersion

libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % log4jVersion
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % log4jVersion
libraryDependencies += "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion

mainClass in Compile := Some("scalapt.MainFrame")
