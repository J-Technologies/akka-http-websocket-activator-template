name := "akka-http-websocket-activator-template"
organization := "Ordina"
version := "1.0"
scalaVersion := "2.11.6"

libraryDependencies ++= {
  val akkaV = "2.3.11"
  val akkaStreamV = "1.0-RC4"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-persistence-experimental" % akkaV,
    
    //test deps
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamV % Test,
    "com.typesafe.akka" %% "akka-stream-testkit-experimental" % akkaStreamV % Test,
    "com.migesok" %% "akka-persistence-in-memory-snapshot-store" % "0.1.1" % Test,
    "org.scalatest" %% "scalatest" % "2.2.5" % Test,
    "junit" % "junit" % "4.10" % Test
  )
}

fork in run := true

resolvers += "migesok at bintray" at "http://dl.bintray.com/migesok/maven"
