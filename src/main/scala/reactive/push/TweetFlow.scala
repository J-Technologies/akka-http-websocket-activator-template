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
import akka.actor.ActorSystem

trait TweetJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat1(User.apply)
  implicit val tweetFormat = jsonFormat2(Tweet.apply)

  private[push] def toMessage(tweet: Tweet): Message = TextMessage.Strict(tweet.toJson.compactPrint)
}

trait TweetSource {
  private[push] val tweetSource: Source[Tweet, ActorRef] = Source.actorPublisher[Tweet](TimelineSourceActor.props)
  
  def tweetsWithHashtag(hashtag: String) = tweetSource filter (_.text contains hashtag)
}

object TweetFlow extends TweetSource with TweetJsonProtocol {
  def websocketFlow(implicit system: ActorSystem): Flow[Message, Message, Unit] = {
    Flow.wrap(Sink.ignore, tweetSource map toMessage)(Keep.left)
  }
}

class HashtagFlow(hashtag: String) extends TweetSource with TweetJsonProtocol {
  def websocketFlow(implicit system: ActorSystem): Flow[Message, Message, Unit] = {
    Flow.wrap(Sink.ignore, tweetsWithHashtag(hashtag) map toMessage)(Keep.left)
  }
}

object HashtagFlow {
  def apply(hashtag: String) = new HashtagFlow(hashtag)
}
