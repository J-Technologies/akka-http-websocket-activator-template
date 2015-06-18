package reactive.tweets.incoming

import akka.actor.{Props, Status}
import akka.persistence.PersistentActor
import reactive.tweets.domain.{Tweet, User}

object TweetPublisherActor {
  def props(user: User): Props = Props(new TweetPublisherActor(user))


}

class TweetPublisherActor(val user: User) extends PersistentActor {

  override def persistenceId = user.name

  override def receiveCommand = {
    case tweet: Tweet =>
      persist(tweet) { event =>
        sender() ! Status.Success
        context.system.eventStream.publish(tweet)
      }

    case msg =>
      throw new UnsupportedOperationException(s"received unexpected message $msg from ${sender()}")
  }

  override def receiveRecover = {
    case tweet: Tweet => println(tweet)
    case _            =>
  }

}
