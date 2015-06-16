package reactive.push

import akka.actor._
import akka.stream.actor.ActorPublisher
import reactive.receive.TimelineActor
import reactive.receive.TimelineActor.Tweet

class TimelineSourceActor extends ActorPublisher[TimelineActor.Tweet] {

  override def preStart = {
    context.system.eventStream.subscribe(self, classOf[TimelineActor.Tweet])
  }

  override def receive = {
    case tweet: Tweet => {
      println(s"Remko: $tweet")
      onNext(tweet)
    }
  }
}

object TimelineSourceActor {
  def props: Props = Props(new TimelineSourceActor())
}

