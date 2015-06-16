package reactive

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import reactive.push.TweetFlow
import reactive.receive.{TimelineActor, TimelineActorManager, User}

import scala.concurrent.duration._
import scala.language.postfixOps

object Main extends App {
  implicit val system = ActorSystem("webapi")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorFlowMaterializer()
  implicit val timeout = Timeout(1000 millis)

  val serverBinding = Http().bindAndHandle(interface = "0.0.0.0", port = 8080, handler = mainFlow)

  def mainFlow: Route = {
    (get & path("post")) {
      complete {
        val saved = system.actorOf(TimelineActorManager.props) ? TimelineActor.Tweet(User("test"), "cool")
        saved.map(_ => "Akka bla bla bla")
      }
    } ~
    get {
      pathEndOrSingleSlash {
        handleWebsocketMessages(TweetFlow.ofAll())
      }
    }

  }
}
