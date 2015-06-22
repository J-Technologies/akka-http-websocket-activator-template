package reactive.tweets.incoming

import akka.actor.{Actor, Props}
import reactive.tweets.domain.{Tweet, User}
import reactive.tweets.incoming.TweetActor.GetLastTen

class TweetActorManager extends Actor {

  override def receive = {
    case tweet: Tweet => forward(tweet, tweet.user.name)

    case lastTen: GetLastTen => forward(lastTen, lastTen.user.name)

    case msg => throw new UnsupportedOperationException(s"received unexpected message $msg from ${sender()}")
  }

  def forward(message: Any, userName: String) = {
    val tweetPublisherActor = context.child(userName).getOrElse(context.actorOf(TweetActor.props(User(userName)), userName))
    tweetPublisherActor forward message
  }
}

object TweetActorManager {
  def props: Props = Props[TweetActorManager]
}
