package reactive.receive

import akka.actor.Props
import akka.actor.Actor 

/** An actor that locates StudentActor and manages their life cycle. */
class TimelineActorManager() extends Actor {

  override def receive = {
    case tweet: TimelineActor.Tweet =>
      val name = tweet.user.name
      val studentActor = context.child(name).getOrElse(context.actorOf(TimelineActor.props(tweet.user), name))
      studentActor forward tweet
    case msg => throw new UnsupportedOperationException(s"received unexpected message $msg from ${sender()}")

  }
}

object TimelineActorManager {
  def props: Props = Props[TimelineActorManager]
}
