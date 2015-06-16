package reactive

import akka.util.Timeout
import akka.stream.ActorFlowMaterializer
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import scala.language.postfixOps
import scala.concurrent.duration._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.get
import akka.http.scaladsl.server.Directives.complete

object Main extends App {
  implicit val system = ActorSystem("webapi")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorFlowMaterializer()
  implicit val timeout = Timeout(1000 millis)

  val serverBinding = Http().bindAndHandle(interface = "0.0.0.0", port = 8080, handler = mainFlow)

  def mainFlow: Route = {
    get {
      complete("Akka bla bla bla")
    }
  }
}
