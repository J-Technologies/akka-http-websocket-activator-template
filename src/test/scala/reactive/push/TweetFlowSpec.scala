package reactive.push

import scala.language.postfixOps

import org.junit.runner.RunWith

import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.ActorFlowMaterializer
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.testkit.scaladsl.TestSource
import reactive.ActorSpec
import reactive.receive.TimelineActor
import reactive.receive.TimelineActorManager
import reactive.receive.User

class TweetFlowSpec extends ActorSpec {

  "A tweetflow websocket " should "not respond to messages" in {
    implicit val materializer = ActorFlowMaterializer()
    val sut = TweetFlow.websocketFlow.runWith(TestSource.probe[Message], TestSink.probe[Message])

    sut._1.sendNext(TextMessage.Strict(""))

    sut._2.request(1)
    sut._2.expectNoMsg()
  }

  it should " send all tweets" in {
    implicit val materializer = ActorFlowMaterializer()
    val sut = TweetFlow.websocketFlow.runWith(TestSource.probe[Message], TestSink.probe[Message])

    val tweet = TimelineActor.Tweet(User("test"), "Hello World!")
    system.eventStream.publish(tweet)

    sut._2.request(1)
    sut._2.expectNext()
    sut._2.expectNoMsg()
  }

  "A hashtag websocket " should " send on tweet without matching hashtag" in {
    implicit val materializer = ActorFlowMaterializer()
    val sut = new HashtagFlow("test-ok").websocketFlow.runWith(TestSource.probe[Message], TestSink.probe[Message])

    val tweet = TimelineActor.Tweet(User("test"), "Hello World! #test-ok")
    system.eventStream.publish(tweet)

    sut._2.request(1)
    sut._2.expectNext()
    sut._2.expectNoMsg()
  }
  
  "A hashtag websocket " should " not send on tweet without matching hashtag" in {
	  implicit val materializer = ActorFlowMaterializer()
			  val sut = new HashtagFlow("test-ok").websocketFlow.runWith(TestSource.probe[Message], TestSink.probe[Message])
			  
			  val tweet = TimelineActor.Tweet(User("test"), "Hello World!")
			  system.eventStream.publish(tweet)
			  
			  sut._2.request(1)
			  sut._2.expectNoMsg()
  }

  def actorRef = system.actorOf(TimelineActorManager.props)

}
