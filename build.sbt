lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging)

name := "akka-http-docker-minimal-seed"

version := "1.0"

scalaVersion := "2.11.8"

packageName in Docker := "akka-http-docker-minimal-seed"
dockerExposedPorts := Seq(5000)

libraryDependencies ++= {
  val akkaV = "2.5.11"
  val scalaTestV = "2.2.6"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http" % "10.0.11",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.11",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.1.0" % Test,
    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "io.kamon" %% "kamon-core" % "1.1.0",
    "io.kamon" %% "kamon-prometheus" % "1.0.0"
//    "io.kamon" %% "kamon-akka-http-2.5" % "1.0.1"
  )
}
unmanagedResourceDirectories in Compile += {
  baseDirectory.value / "src/main/resources"
}