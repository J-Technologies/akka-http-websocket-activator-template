package reactive.push

import akka.actor._
import akka.stream.actor.ActorPublisher
import reactive.receive.TimelineActor
import reactive.receive.TimelineActor.Tweet

class HashtagSourceActor(hashtag: String) extends ActorPublisher[TimelineActor.Tweet] {

  override def preStart = {
    context.system.eventStream.subscribe(self, classOf[TimelineActor.Tweet])
  }

  override def receive = {
    case tweet: Tweet => {
      if (tweet.text.contains(hashtag)) {
        onNext(tweet)
      }
    }
  }
}

object HashtagSourceActor {
  def props(hashtag: String): Props = Props(new HashtagSourceActor(hashtag))
}

