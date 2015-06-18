package reactive.tweets.incoming

import akka.actor.{Actor, Props}
import reactive.tweets.domain.{Tweet, User}
import reactive.tweets.incoming.TweetPublisherActor.GetLastTen

class TweetPublisherActorManager extends Actor {

  override def receive = {
    case tweet: Tweet => forward(tweet, tweet.user.name)

    case lastTen: GetLastTen => forward(lastTen, lastTen.user.name)

    case msg => throw new UnsupportedOperationException(s"received unexpected message $msg from ${sender()}")
  }

  def forward(message: Any, userName: String) = {
    val timelineActor = context.child(userName).getOrElse(context.actorOf(TweetPublisherActor.props(User(userName)), userName))
    timelineActor forward message
  }
}

object TweetPublisherActorManager {
  def props: Props = Props[TweetPublisherActorManager]
}
