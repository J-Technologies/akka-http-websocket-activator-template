package reactive.tweets.outgoing

import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl._
import reactive.tweets.domain.Tweet
import reactive.tweets.marshalling.TweetJsonProtocol

trait TweetSource extends TweetJsonProtocol {
  private[outgoing] val tweetSource: Source[Tweet, ActorRef] = Source.actorPublisher[Tweet](TweetsSourceActor.props)
  type TweetFilter = Tweet => Boolean
  
  def tweetFlow(tweetFilter: TweetFilter): Flow[Message, Message, Unit] =
    Flow.wrap(Sink.ignore, tweetSource filter tweetFilter map toMessage)(Keep.left)

  def tweetFlowOfUser(userName: String) = tweetFlow(_.user.name == userName)

  def tweetFlowOfAll = tweetFlow(_ => true)

  def tweetFlowOfHashTag(hashTag: String) = tweetFlow(_.text contains hashTag)
}