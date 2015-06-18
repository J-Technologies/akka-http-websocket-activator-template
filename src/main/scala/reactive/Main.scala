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
import reactive.tweets.incoming.TweetPublisherActor.{GetLastTen, LastTenResponse}
import reactive.tweets.incoming.TweetPublisherActorManager
import reactive.tweets.marshalling.TweetJsonProtocol
import reactive.tweets.outgoing.{HashtagFlow, TweetFlow, UserFlow}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

object Main extends App with TweetJsonProtocol {
  implicit val system = ActorSystem("webapi")
  implicit val executor = system.dispatcher
  implicit val timeout = Timeout(1000 millis)

  implicit val materializer = ActorFlowMaterializer()
  val serverBinding = Http().bindAndHandle(interface = "0.0.0.0", port = 8080, handler = mainFlow)

  def mainFlow(implicit system: ActorSystem, timeout: Timeout, executor: ExecutionContext): Route = {
    def getLatestTweetsOfUser = (pathPrefix("users") & path(Segment)) { userName =>
      complete {
        val response = (system.actorOf(TweetPublisherActorManager.props) ? GetLastTen(User(userName))).asInstanceOf[Future[LastTenResponse]]
        response map (_.lastTen)
      }
    }

    def websocketTweetsOfUser = (pathPrefix("users") & path(Segment)) { userName =>
      handleWebsocketMessages(UserFlow(userName).websocketFlow)
    }

    def websocketAllTweets = path("all") {
      handleWebsocketMessages(TweetFlow.websocketFlow)
    }

    def websocketTweetsWithHashtag = {
      (pathPrefix("hashtag") & path(Segment)) { hashtag =>
        handleWebsocketMessages(HashtagFlow(hashtag).websocketFlow)
      }
    }

    def postTweet = {
      entity(as[Tweet]) { tweet =>
        complete {
          (system.actorOf(TweetPublisherActorManager.props) ? tweet).map(_ => StatusCodes.NoContent)
        }
      }
    }

    // Frontend
    def index = (path("") | pathPrefix("index.htm")) { getFromResource("index.html") }
    def css = (pathPrefix("css") & path(Segment)) { resource => getFromResource(s"css/$resource") }
    def fonts = (pathPrefix("fonts") & path(Segment)) { resource => getFromResource(s"fonts/$resource") }
    def img = (pathPrefix("img") & path(Segment)) { resource => getFromResource(s"img/$resource") }
    def js = (pathPrefix("js") & path(Segment)) { resource => getFromResource(s"js/$resource") }

    get {
       index ~ css ~ fonts ~ img ~ js ~
       getLatestTweetsOfUser ~
       websocketAllTweets ~
       websocketTweetsWithHashtag ~
       websocketTweetsOfUser
    } ~
    post {
      postTweet
    }

  }


}
