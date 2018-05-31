val scalaPTVersion = "1.0-SNAPSHOT"

val scalaLangVersion = "2.12.4"

val avro4sVersion = "1.8.0"
val catsVersion = "1.1.0"
val circeVersion = "0.10.0-M1"
val log4jVersion = "2.11.0"
val monixVersion = "3.0.0-RC1"
val scalaLogVersion = "3.9.0"
val scoptVersion = "3.7.0"
val slf4jVersion = "1.7.25"

// Enable this to obviate the need for traverseU etc.
// Required for Cats.

scalacOptions += "-Ypartial-unification"

// The root project is implicit, so we don't have to define it.
// We do need to prevent publishing for it, though:
publishArtifact := false
publish := {}

lazy val commonSettings = Seq(
    version := scalaPTVersion,
    scalaVersion := scalaLangVersion
)

lazy val core = Project(id = "ScalaPT-Core", base = file("core"))
    .settings(commonSettings: _*)
    .settings(name := "ScalaPT-Core")
    .settings(
        libraryDependencies ++= Seq(
            "io.circe" %% "circe-core",
            "io.circe" %% "circe-generic",
            "io.circe" %% "circe-parser"
        ).map(_ % circeVersion)
    ).settings(
        libraryDependencies ++= Seq(
            "org.typelevel" %% "cats-core",
            "org.typelevel" %% "cats-free"
        ).map(_ % catsVersion)
    ).settings(
        libraryDependencies +=
            "com.typesafe.scala-logging" %% "scala-logging" % scalaLogVersion
    ).settings(
        libraryDependencies ++= Seq(
            "io.monix" %% "monix"
        ).map(_ % monixVersion)
    ).settings(
        libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-core" % avro4sVersion
    )

lazy val app = Project(id = "ScalaPT-App", base = file("app"))
    .settings(commonSettings: _*)
    .settings(name := "ScalaPT-App")
    .settings(mainClass in Compile := Some("scalapt.app.Main"))
    .settings(
        libraryDependencies +=
            "com.github.scopt" %% "scopt" % scoptVersion
    ).settings(
        libraryDependencies +=
            "org.slf4j" % "slf4j-api" % slf4jVersion % "runtime"
    ).settings(
        libraryDependencies ++= Seq(
            "org.apache.logging.log4j" % "log4j-core",
            "org.apache.logging.log4j" % "log4j-api",
            "org.apache.logging.log4j" % "log4j-slf4j-impl"
        ).map(_ % log4jVersion % "runtime")
    ).dependsOn(core)
