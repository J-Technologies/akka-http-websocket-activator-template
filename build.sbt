name := "akka-http-websocket-activator-template"
organization := "Ordina"
version := "1.0"
scalaVersion := "2.11.6"

libraryDependencies ++= {
  val akkaV = "2.4.2"
  val akkaStreamV = "2.0.3"
  val akkaPersistenceV = "2.3.6"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-persistence-experimental" % akkaPersistenceV,
    
    //test deps
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % "2.4.2-RC3" % Test,
    "com.typesafe.akka" %% "akka-stream-testkit-experimental" % akkaStreamV % Test,
    "com.migesok" %% "akka-persistence-in-memory-snapshot-store" % "0.1.1" % Test,
    "org.scalatest" %% "scalatest" % "2.2.5" % Test,
    "junit" % "junit" % "4.10" % Test
  )
}

fork in run := true

resolvers += "migesok at bintray" at "http://dl.bintray.com/migesok/maven"
resolvers += Resolver.file("Local repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns)