package reactive.tweets.outgoing

import akka.actor._
import akka.stream.actor.ActorPublisher
import reactive.tweets.domain.Tweet

class TweetsSourceActor extends ActorPublisher[Tweet] {

  override def preStart = {
    context.system.eventStream.subscribe(self, classOf[Tweet])
  }

  override def receive = {
    case tweet: Tweet => onNext(tweet)
  }
}

object TweetsSourceActor {
  def props: Props = Props(new TweetsSourceActor())
}

