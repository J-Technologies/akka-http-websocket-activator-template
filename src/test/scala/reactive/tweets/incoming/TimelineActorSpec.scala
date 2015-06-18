package reactive.tweets.incoming

import akka.actor.Status
import reactive.ActorTestUtils
import reactive.tweets.domain.{ Tweet, User }
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import reactive.tweets.incoming.TweetPublisherActor.GetLastTen
import reactive.tweets.incoming.TweetPublisherActor.LastTenResponse

class TimelineActorSpec extends ActorTestUtils {

  def timelineActorManager = system.actorOf(TweetPublisherActorManager.props)

  val tweet = Tweet(User("test"), "Hello World!")
  val tweetLatest = Tweet(User("test"), "Hello World! again")

  "A Timeline Actor Manager " should "persist the tweet successfully" in {
    within(500 millis) {
      timelineActorManager ! tweet
      expectMsg(Status.Success)
      expectNoMsg()
    }
  }

  "A Timeline Actor " should "broadcast a succesfully saved tweet" in {
    within(500 millis) {
      system.eventStream.subscribe(testActor, classOf[Tweet])

      timelineActorManager ! tweet
      expectMsg(Status.Success)
      expectMsg(tweet)
      expectNoMsg()

      system.eventStream.unsubscribe(testActor)
    }
  }

  it should "save the latest tweets" in {
    within(500 millis) {

      timelineActorManager ! tweetLatest
      expectMsg(Status.Success)
      timelineActorManager ! GetLastTen(tweet.user)

      expectMsg(LastTenResponse(List(tweetLatest, tweet, tweet)))
    }
  }

  it should "save only the latest ten tweets" in {
    within(500 millis) {

      for (i <- 1 to 10) yield {
        timelineActorManager ! tweetLatest
        expectMsg(Status.Success)
      }
      
      timelineActorManager ! GetLastTen(tweet.user)

      expectMsg(LastTenResponse((1 to 10).map(_ => tweetLatest).toList))
    }
  }
}
