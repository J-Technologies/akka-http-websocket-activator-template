package reactive.tweets.outgoing

import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl._
import reactive.tweets.domain.Tweet
import reactive.tweets.marshalling.TweetJsonProtocol

trait TweetFlow extends TweetJsonProtocol {
  private val tweetSource: Source[Tweet, ActorRef] = Source.actorPublisher[Tweet](TweetPublisher.props)
  type TweetFilter = Tweet => Boolean
  
  private def tweetFlow(tweetFilter: TweetFilter): Flow[Message, Message, Unit] =
    Flow.fromSinkAndSource(Sink.ignore, tweetSource filter tweetFilter map toMessage)

  def tweetFlowOfUser(userName: String) = tweetFlow(_.user.name.equalsIgnoreCase(userName))

  def tweetFlowOfAll = tweetFlow(_ => true)

  def tweetFlowWithHashTag(hashTag: String): Flow[Message, Message, Unit] = ???
}