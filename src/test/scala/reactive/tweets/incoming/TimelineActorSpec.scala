package reactive.tweets.incoming

import akka.actor.Status
import reactive.ActorTestUtils
import reactive.tweets.domain.{Tweet, User}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class TimelineActorSpec extends ActorTestUtils {

  def timelineActorManager = system.actorOf(TweetPublisherActorManager.props)

  "A Timeline Actor Manager " should "persist the tweet successfully" in {
    within(500 millis) {
      timelineActorManager ! Tweet(User("test"), "Hello World!")
      expectMsg(Status.Success)
      expectNoMsg()
    }
  }
  
  "A Timeline Actor " should "broadcast a succesfully saved tweet" in {
    within(500 millis) {
      system.eventStream.subscribe(testActor, classOf[Tweet])
      val tweet = Tweet(User("test"), "Hello World!")

      timelineActorManager ! tweet
      expectMsg(Status.Success)
      expectMsg(tweet)
      expectNoMsg()
    }
  }

}
