name := "caliban-akka-persistence"

version := "0.1"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.3"
lazy val akkaHttpVersion = "10.1.11"
lazy val calibanVersion = "0.5.1"

libraryDependencies ++= Seq(
  "com.github.ghostdogpr" %% "caliban" % calibanVersion,
  "com.github.ghostdogpr" %% "caliban-akka-http" % calibanVersion,

  "io.scalaland" %% "chimney" % "0.4.1",

  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,

  "org.iq80.leveldb" % "leveldb" % "0.7" % Runtime,
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8" % Runtime,
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
)
