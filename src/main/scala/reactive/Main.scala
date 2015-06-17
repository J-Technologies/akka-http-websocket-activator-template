package reactive

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.server.ConjunctionMagnet.fromDirective
import akka.http.scaladsl.server.Directive.addByNameNullaryApply
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Directives.enhanceRouteWithConcatenation
import akka.http.scaladsl.server.Directives.get
import akka.http.scaladsl.server.Directives.handleWebsocketMessages
import akka.http.scaladsl.server.Directives.path
import akka.http.scaladsl.server.Directives.pathEndOrSingleSlash
import akka.http.scaladsl.server.Directives.segmentStringToPathMatcher
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import akka.pattern.ask
import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import reactive.push.TweetFlow
import reactive.receive.TimelineActor
import reactive.receive.TimelineActorManager
import reactive.receive.User

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
        handleWebsocketMessages(TweetFlow.websocketFlow)
      }
    }
  }
}
