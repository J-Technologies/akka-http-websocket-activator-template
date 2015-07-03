package reactive.tweets.outgoing

import akka.actor._
import akka.stream.actor.ActorPublisher
import reactive.tweets.domain.Tweet

class TweetPublisher extends ActorPublisher[Tweet] {

  override def preStart = {
    context.system.eventStream.subscribe(self, classOf[Tweet])
  }

  override def receive = {
    case tweet: Tweet =>
      //We do not send tweets if a client is not reading from the stream fast enough.
      if (isActive && totalDemand > 0)
        onNext(tweet)
  }

}

object TweetPublisher {
  def props: Props = Props(new TweetPublisher())
}

