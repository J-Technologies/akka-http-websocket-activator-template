package reactive.push

import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import reactive.receive.TimelineActor.Tweet
import reactive.receive.User
import spray.json.DefaultJsonProtocol
import spray.json.pimpAny

trait TweetJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat1(User.apply)
  implicit val tweetFormat = jsonFormat2(Tweet.apply)
}

object TweetFlow extends TweetJsonProtocol {
  private def toMessage(tweet: Tweet): Message = TextMessage.Strict(tweet.toJson.compactPrint)

  private val tweetSource: Source[Tweet, ActorRef] = Source.actorPublisher[Tweet](TimelineSourceActor.props)
  private val websocketMessageSource = tweetSource map toMessage

  def websocketFlow: Flow[Message, Message, Unit] = {
    Flow.wrap(Sink.ignore, websocketMessageSource)(Keep.left)
  }
}