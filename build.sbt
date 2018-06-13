organization := "com.github.pjfanning"

name := "micrometer-kamon"

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.11.12", "2.12.6")

scalacOptions += "-target:jvm-1.8"

resolvers += Resolver.bintrayRepo("kamon-io", "releases")

val kamonVersion = "1.1.2"
val micrometerVersion = "1.0.5"

libraryDependencies ++= Seq(
  "io.micrometer" % "micrometer-core" % micrometerVersion,
  "io.kamon" %% "kamon-core" % kamonVersion,
  "com.typesafe" % "config" % "1.3.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"
)

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

parallelExecution in Test := false
logBuffered := false

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

homepage := Some(url("https://github.com/pjfanning/micrometer-kamon"))

licenses := Seq("The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

releasePublishArtifactsAction := PgpKeys.publishSigned.value

pomExtra := (
  <scm>
    <url>git@github.com:pjfanning/micrometer-kamon.git</url>
    <connection>scm:git:git@github.com:pjfanning/micrometer-kamon.git</connection>
  </scm>
    <developers>
      <developer>
        <id>pjfanning</id>
        <name>PJ Fanning</name>
        <url>https://github.com/pjfanning</url>
      </developer>
    </developers>
)
