val scalaPTVersion = "1.0"

val scalaLangVersion = "2.12.0"

val catsVersion = "0.8.1"
val circeVersion = "0.6.0"
val log4jVersion = "2.7"
val monixVersion = "2.1.0"
val scalaLogVersion = "3.5.0"

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
    )

lazy val app = Project(id = "ScalaPT-App", base = file("app"))
    .settings(commonSettings: _*)
    .settings(name := "ScalaPT-App")
    .settings(mainClass in Compile := Some("scalapt.MainFrame"))
    .dependsOn(core)
