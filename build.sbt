name := "micrometer-kamon"

scalaVersion := "2.12.4"

scalacOptions += "-target:jvm-1.8"

resolvers += Resolver.bintrayRepo("kamon-io", "releases")

val kamonVersion = "1.0.1"
val micrometerVersion = "1.0.0-rc.9"

libraryDependencies ++= Seq(
  "io.micrometer" % "micrometer-core" % micrometerVersion,
  "io.kamon" %% "kamon-core" % kamonVersion
)
