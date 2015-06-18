package reactive.tweets.incoming

import akka.actor.{ Actor, Props }
import reactive.tweets.domain.Tweet
import reactive.tweets.incoming.TweetPublisherActor.GetLastTen
import reactive.tweets.domain.WithUser

class TweetPublisherActorManager extends Actor {

  override def receive = {
    case tweet: WithUser =>
      val name = tweet.user.name
      val timelineActor = context.child(name).getOrElse(context.actorOf(TweetPublisherActor.props(tweet.user), name))
      timelineActor forward tweet

    case msg =>
      throw new UnsupportedOperationException(s"received unexpected message $msg from ${sender()}")

  }
}

object TweetPublisherActorManager {
  def props: Props = Props[TweetPublisherActorManager]
}
