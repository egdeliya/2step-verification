import sbt._

object Dependencies {

  object version {
    val phantomVersion = "2.41.0"
    val logback = "1.2.3"
    val logging = "3.9.0"
    val akkaHttp = "10.0.9"
    val akka = "2.5.19"
    val akkaSession = "0.5.6"
  }

  val configTypesafe = Seq (
    "com.typesafe" % "config" % "1.3.2"
  )
  
  val phantomCassandra = Seq(
    "com.outworkers"  %% "phantom-dsl" % version.phantomVersion
  )
  
  val scalaTest = Seq (
    "org.mockito" % "mockito-all" % "1.8.4",
    "org.scalactic" %% "scalactic" % "3.0.4",
    "org.scalatest" %% "scalatest" % "3.0.4",
    "org.scalacheck" %% "scalacheck" % "1.13.4"

  )

  val logging = Seq (
    "ch.qos.logback" % "logback-classic" % version.logback,
    "com.typesafe.scala-logging" %% "scala-logging" % version.logging
  )

  val akka = Seq(
    "com.typesafe.akka" %% "akka-http" % version.akkaHttp,
    "com.typesafe.akka" %% "akka-stream" % "2.5.19",
    "com.typesafe.akka" %% "akka-http-spray-json" % version.akkaHttp,
    "com.typesafe.akka" %% "akka-actor" % version.akka,
    "com.typesafe.akka" %% "akka-http-testkit" % version.akkaHttp,
    "com.softwaremill.akka-http-session" %% "core" % version.akkaSession
  )

  val bcrypt = Seq(
    "org.mindrot" % "jbcrypt" % "0.3m"
  )

  val sms = Seq(
    "com.nexmo" % "client" % "4.2.1"
  )
  
  val redis = Seq(
    "net.debasishg" %% "redisclient" % "3.9"
  )
  
  val jwtSession = Seq(
    "com.pauldijou" %% "jwt-play" % "2.1.0"
  )
}