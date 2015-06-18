package reactive.tweets.outgoing

import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import reactive.tweets.domain.{Tweet, User}
import spray.json.{DefaultJsonProtocol, pimpAny}

trait TweetJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat1(User.apply)
  implicit val tweetFormat = jsonFormat2(Tweet.apply)

  private[outgoing] def toMessage(tweet: Tweet): Message = TextMessage.Strict(tweet.toJson.compactPrint)
}

trait TweetSource {
  private[outgoing] val tweetSource: Source[Tweet, ActorRef] = Source.actorPublisher[Tweet](TweetsSourceActor.props)
  def filteredTweetSource(tweetFilter: Tweet => Boolean) = tweetSource filter tweetFilter
}

object TweetFlow extends TweetSource with TweetJsonProtocol {
  def websocketFlow: Flow[Message, Message, Unit] = Flow.wrap(Sink.ignore, tweetSource map toMessage)(Keep.left)
}

class HashtagFlow(hashtag: String) extends TweetSource with TweetJsonProtocol {
  def websocketFlow: Flow[Message, Message, Unit] = {
    Flow.wrap(Sink.ignore, filteredTweetSource(_.text contains hashtag) map toMessage)(Keep.left)
  }
}

object HashtagFlow {
  def apply(hashtag: String) = new HashtagFlow(hashtag)
}

class UserFlow(userName: String) extends TweetSource with TweetJsonProtocol {
  def websocketFlow: Flow[Message, Message, Unit] = {
    Flow.wrap(Sink.ignore, filteredTweetSource(_.user.name == userName) map toMessage)(Keep.left)
  }
}

object UserFlow {
  def apply(userName: String) = new UserFlow(userName)
}