import Dependencies._

name := "2stepAuthorization"

scalaVersion := "2.12.8"

libraryDependencies ++= configTypesafe
libraryDependencies ++= scalaTest
libraryDependencies ++= logging
libraryDependencies ++= phantomCassandra
libraryDependencies ++= akka
libraryDependencies ++= bcrypt
libraryDependencies ++= sms
libraryDependencies ++= redis