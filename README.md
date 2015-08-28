This branch is based on a snapshot version of akka.
Use this steps to build akka.

* git clone https://github.com/akka/akka.git
* cd akka
* git checkout -b release-2.3-dev remotes/origin/release-2.3-dev
* sbt compile -Dakka.scalaVersion=2.11.6
* sbt test -Dakka.scalaVersion=2.11.6
* sbt publish-local -Dakka.scalaVersion=2.11.6 -Dakka.scaladoc.diagrams=false