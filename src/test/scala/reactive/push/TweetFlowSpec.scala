package reactive.receive

import reactive.ActorSpec
import akka.actor.Status
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.language.postfixOps
import scala.concurrent.duration.DurationInt

@RunWith(classOf[JUnitRunner])
class TweetFlowSpec extends ActorSpec {

  "A timelineactormanager " should "persist the statement successfully" in {
    within(500 millis) {
      actorRef ! TimelineActor.Tweet(User("test"), "Hello World!")
      expectMsg(Status.Success)
      expectNoMsg()
    }
  }

  def actorRef = system.actorOf(TimelineActorManager.props)

}
