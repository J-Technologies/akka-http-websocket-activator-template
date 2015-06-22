package reactive.tweets.incoming

import akka.actor.{Props, Status, actorRef2Scala}
import akka.persistence.{PersistentActor, SnapshotOffer}
import reactive.tweets.domain.{Tweet, User}
import reactive.tweets.incoming.TweetActor.{GetLastTen, LastTenResponse}
import akka.persistence.SaveSnapshotSuccess

object TweetActor {
  def props(user: User): Props = Props(new TweetActor(user))

  case class GetLastTen(user: User)
  case class LastTenResponse(lastTen: List[Tweet])
}

class TweetActor(val user: User) extends PersistentActor {
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

      
    case s: SaveSnapshotSuccess => 
      
    case msg =>
      throw new UnsupportedOperationException(s"received unexpected message $msg from ${sender()}")
  }

  override def receiveRecover = {
    case tweet: Tweet =>
      addToLatest(tweet)
    case SnapshotOffer(_, latest: List[Tweet] @unchecked) =>
      latestTweets = latest
    case _ =>
  }

  private def addToLatest(tweet: Tweet) {
    latestTweets = tweet :: latestTweets

    if (latestTweets.length > 100) {
      latestTweets = latestTweets.take(10)
      saveSnapshot(latestTweets)
    }
  }
}
