name := "kafka-helper"

version := "0.1"

scalaVersion := "2.12.6"

resolvers += "confluent" at "http://packages.confluent.io/maven/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",

  "com.lightbend" %% "kafka-streams-scala" % "0.2.1",
  "org.apache.kafka" % "kafka-streams" % "1.1.0",
  "io.confluent" % "kafka-connect-avro-converter" % "3.2.1",
  "io.confluent" % "kafka-streams-avro-serde" % "3.3.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.6",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",

  "com.typesafe.akka" %% "akka-stream-kafka" % "1.0.4",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",

  "org.mockito" % "mockito-all" % "1.8.4" % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "com.madewithtea" %% "mockedstreams" % "1.7.0" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test,
  "org.apache.kafka" % "kafka-streams-test-utils" % "1.1.0" % Test,
  "com.github.tomakehurst" % "wiremock" % "2.18.0" % Test
)

sourceGenerators in Compile += (avroScalaGenerateSpecific in Compile).taskValue
