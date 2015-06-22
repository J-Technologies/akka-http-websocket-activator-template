package reactive

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive.{addByNameNullaryApply, addDirectiveApply}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatcher.segmentStringToPathMatcher
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import akka.pattern.ask
import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import reactive.tweets.domain.{Tweet, User}
import reactive.tweets.incoming.TweetActor.{GetLastTen, LastTenResponse}
import reactive.tweets.incoming.TweetActorManager
import reactive.tweets.outgoing.TweetFlow

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object Main extends App with TweetFlow {
  implicit val system = ActorSystem("webapi")
  implicit val executor = system.dispatcher
  implicit val timeout = Timeout(1000 millis)

  implicit val materializer = ActorFlowMaterializer()
  val serverBinding = Http().bindAndHandle(interface = "0.0.0.0", port = 8080, handler = mainFlow)

  def mainFlow(implicit system: ActorSystem, timeout: Timeout, executor: ExecutionContext): Route = {
    def getLatestTweetsOfUser = (pathPrefix("users") & path(Segment)) { userName =>
      complete {
        (system.actorOf(TweetActorManager.props) ? GetLastTen(User(userName)))
          .mapTo[LastTenResponse]
          .map(_.lastTen)
      }
    }

    def tweetsOfUserSocket = (pathPrefix("users") & path(Segment)) { userName =>
      handleWebsocketMessages(tweetFlowOfUser(userName))
    }

    def allTweetsSocket = path("all") {
      handleWebsocketMessages(tweetFlowOfAll)
    }

    // TODO Add implementation (Part 2 of tutorial)
    def tweetsWithHashTagSocket = ???

    def addTweet = {
      post {
        entity(as[Tweet]) { tweet =>
          complete {
            (system.actorOf(TweetActorManager.props) ? tweet).map(_ => StatusCodes.NoContent)
          }
        }
      }
    }

    // Frontend
    def index = (path("") | pathPrefix("index.htm")) {
      getFromResource("index.html")
    }
    def css = (pathPrefix("css") & path(Segment)) { resource => getFromResource(s"css/$resource") }
    def fonts = (pathPrefix("fonts") & path(Segment)) { resource => getFromResource(s"fonts/$resource") }
    def img = (pathPrefix("img") & path(Segment)) { resource => getFromResource(s"img/$resource") }
    def js = (pathPrefix("js") & path(Segment)) { resource => getFromResource(s"js/$resource") }

    get {
      index ~ css ~ fonts ~ img ~ js
    } ~
      // REST endpoints
      pathPrefix("resources") {
        pathPrefix("tweets") {
          get {
            getLatestTweetsOfUser
          } ~
            addTweet
        }
      } ~
      // Websocket endpoints
      pathPrefix("ws") {
        pathPrefix("tweets") {
          get {
            allTweetsSocket ~
              tweetsOfUserSocket
            // TODO Call hash tag functionality from here
          }
        }
      }
  }

}
