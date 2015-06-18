package reactive

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.server.Directive.{addByNameNullaryApply, addDirectiveApply}
import akka.http.scaladsl.server.Directives.{complete, enhanceRouteWithConcatenation, get, handleWebsocketMessages, path, pathEndOrSingleSlash, pathPrefix}
import akka.http.scaladsl.server.PathMatcher.segmentStringToPathMatcher
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import akka.pattern.ask
import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import reactive.tweets.domain.{Tweet, User}
import reactive.tweets.incoming.TweetPublisherActorManager
import reactive.tweets.outgoing.{UserFlow, HashtagFlow, TweetFlow}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object Main extends App {
  implicit val system = ActorSystem("webapi")
  implicit val executor = system.dispatcher
  implicit val timeout = Timeout(1000 millis)

  implicit val materializer = ActorFlowMaterializer()
  val serverBinding = Http().bindAndHandle(interface = "0.0.0.0", port = 8080, handler = mainFlow)

  def mainFlow(implicit system: ActorSystem, timeout: Timeout, executor: ExecutionContext): Route = {
    (get & pathPrefix("post") & path(Segment)) { userName =>
      complete {
        val saved = system.actorOf(TweetPublisherActorManager.props) ? Tweet(User(userName), "cool")
        saved.map(_ => "Akka bla bla bla")
      }
    } ~
      get {
         websocketAllTweets ~
         websocketTweetsWithHashtag ~
         websocketTweetsOfUser
      }
  }

  private def websocketTweetsOfUser = (pathPrefix("users") & path(Segment)) { userName =>
    handleWebsocketMessages(UserFlow(userName).websocketFlow)
  }

  private def websocketAllTweets = pathEndOrSingleSlash {
    handleWebsocketMessages(TweetFlow.websocketFlow)
  }

  private def websocketTweetsWithHashtag = {
    (pathPrefix("hashtag") & path(Segment)) { hashtag =>
      handleWebsocketMessages(HashtagFlow(hashtag).websocketFlow)
    }
  }
}
