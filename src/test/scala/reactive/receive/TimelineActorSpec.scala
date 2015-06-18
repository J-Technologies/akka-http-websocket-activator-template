package reactive.receive

import akka.actor.Status
import reactive.ActorTestUtils
import reactive.receive.TimelineActor.Tweet

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class TimelineActorSpec extends ActorTestUtils {

  "A timelineactormanager " should "persist the statement successfully" in {
    within(500 millis) {
      actorRef ! TimelineActor.Tweet(User("test"), "Hello World!")
      expectMsg(Status.Success)
      expectNoMsg()
    }
  }
  
  "A timelineactor " should "broadcast a succesfully saved tweet" in {
    within(500 millis) {
      system.eventStream.subscribe(testActor, classOf[Tweet])
      val tweet = TimelineActor.Tweet(User("test"), "Hello World!")
      
      actorRef ! tweet
      expectMsg(Status.Success)
      expectMsg(tweet)
      expectNoMsg()
    }
  }
  
  def actorRef = system.actorOf(TimelineActorManager.props)

}
