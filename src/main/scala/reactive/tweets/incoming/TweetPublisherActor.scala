package reactive.tweets.incoming

import akka.actor.Props
import akka.actor.Status
import akka.actor.actorRef2Scala
import akka.persistence.PersistentActor
import akka.persistence.SnapshotOffer
import reactive.tweets.domain.Tweet
import reactive.tweets.domain.User
import reactive.tweets.incoming.TweetPublisherActor.GetLastTen
import reactive.tweets.incoming.TweetPublisherActor.LastTenResponse
import reactive.tweets.domain.WithUser

object TweetPublisherActor {
  def props(user: User): Props = Props(new TweetPublisherActor(user))

  case class GetLastTen(user: User) extends WithUser
  case class LastTenResponse(lastTen: List[Tweet])
}

class TweetPublisherActor(val user: User) extends PersistentActor {
  override def persistenceId = user.name
  var latestTweets = List[Tweet]()

  override def receiveCommand = {
    case tweet: Tweet =>
      persist(tweet) { event =>
        sender() ! Status.Success
        context.system.eventStream.publish(tweet)
      }

    case GetLastTen(_) =>
      sender() ! LastTenResponse(latestTweets.take(10))

    case msg =>
      throw new UnsupportedOperationException(s"received unexpected message $msg from ${sender()}")
  }

  override def receiveRecover = {
    case tweet: Tweet =>
      addToLastTen(tweet)
    case SnapshotOffer(_, latest: List[Tweet] @unchecked) =>
      latestTweets = latest
    case _ =>
  }

  private def addToLastTen(tweet: Tweet) {
    latestTweets = tweet :: latestTweets

    if (latestTweets.length > 100) {
      latestTweets = latestTweets.take(10)
      saveSnapshot(latestTweets)
    }
  }
}
