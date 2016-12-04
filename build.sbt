val scalaPTVersion = "1.0-SNAPSHOT"

val scalaLangVersion = "2.11.8"

val avro4sVersion = "1.6.2"
val catsVersion = "0.7.2"
val circeVersion = "0.5.4"
val log4jVersion = "2.7"
val monixVersion = "2.0.6"
val scalaLogVersion = "3.5.0"
val scoptVersion = "3.5.0"

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
        libraryDependencies += "org.typelevel" %% "cats" % catsVersion
    ).settings(
        libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % scalaLogVersion
    ).settings(
        libraryDependencies ++= Seq(
            "org.apache.logging.log4j" % "log4j-core",
            "org.apache.logging.log4j" % "log4j-api",
            "org.apache.logging.log4j" % "log4j-slf4j-impl"
        ).map(_ % log4jVersion)
    ).settings(
        libraryDependencies ++= Seq(
            "io.monix" %% "monix",
            "io.monix" %% "monix-cats"
        ).map(_ % monixVersion)
    ).settings(
        libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-core" % avro4sVersion
    )

lazy val app = Project(id = "ScalaPT-App", base = file("app"))
    .settings(commonSettings: _*)
    .settings(name := "ScalaPT-App")
    .settings(mainClass in Compile := Some("scalapt.MainFrame"))
    .settings(
        libraryDependencies += "com.github.scopt" %% "scopt" % scoptVersion
    ).dependsOn(core)
