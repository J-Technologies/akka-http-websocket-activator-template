package reactive.receive

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import org.junit.runner.RunWith

import akka.actor.Status
import akka.actor.actorRef2Scala
import reactive.ActorSpec
import reactive.receive.TimelineActor.Tweet

class TimelineActorSpec extends ActorSpec {

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
