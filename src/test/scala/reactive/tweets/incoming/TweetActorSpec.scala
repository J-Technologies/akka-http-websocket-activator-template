package reactive.tweets.incoming

import akka.actor.Status
import reactive.ActorTestUtils
import reactive.tweets.domain.{ Tweet, User }
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import reactive.tweets.incoming.TweetActor.GetLastTen
import reactive.tweets.incoming.TweetActor.LastTenResponse

class TweetActorSpec extends ActorTestUtils {

  def tweetActorManager = system.actorOf(TweetActorManager.props)

  val tweet = Tweet(User("test"), "Hello World!")
  val tweetLatest = Tweet(User("test"), "Hello World! again")

  "The actor manager" should "forward the tweet to the persistent actor" in {
    within(500 millis) {
      tweetActorManager ! tweet
      expectMsg(Status.Success)
      expectNoMsg()
    }
  }

  "The persistent actor" should "broadcast a successfully saved tweet" in {
    within(500 millis) {
      system.eventStream.subscribe(testActor, classOf[Tweet])

      tweetActorManager ! tweet
      expectMsg(Status.Success)
      expectMsg(tweet)
      expectNoMsg()

      system.eventStream.unsubscribe(testActor)
    }
  }

  it should "save the latest tweets" in {
    within(500 millis) {

      tweetActorManager ! tweetLatest
      expectMsg(Status.Success)
      tweetActorManager ! GetLastTen(tweet.user)

      expectMsg(LastTenResponse(List(tweetLatest, tweet, tweet)))
    }
  }

  it should "save only the latest ten tweets" in {
    within(500 millis) {

      for (i <- 1 to 100) yield {
        tweetActorManager ! tweetLatest
        expectMsg(Status.Success)
      }

      tweetActorManager ! GetLastTen(tweet.user)

      expectMsg(LastTenResponse((1 to 10).map(_ => tweetLatest).toList))
    }
  }
  
  it should "recover with the latest messages" in {
    within(500 millis) {

      val user = system.actorOf(TweetActor.props(tweet.user))
      system.stop(user)
      tweetActorManager ! GetLastTen(tweet.user)

      expectMsg(LastTenResponse((1 to 10).map(_ => tweetLatest).toList))
    }
  }
}
