package reactive.push

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.ActorFlowMaterializer
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import reactive.ActorTestUtils
import reactive.receive.{TimelineActor, User}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class TweetFlowSpec extends ActorTestUtils {
  implicit val materializer = ActorFlowMaterializer()
  private val noMessageTimeout = 100 millis

  "The flow for tweets" should "ignore incoming messages" in {
    val sut = TweetFlow.websocketFlow.runWith(TestSource.probe[Message], TestSink.probe[Message])
    val (mockSource, mockSink) = sut

    mockSource.sendNext(TextMessage.Strict("Should be ignored"))

    mockSink.request(1)
    mockSink.expectNoMsg(noMessageTimeout)
  }

  it should "forward all tweets published to the event stream" in {
    val sut = TweetFlow.websocketFlow.runWith(TestSource.probe[Message], TestSink.probe[Message])
    val (_, mockSink) = sut
    
    val tweet = TimelineActor.Tweet(User("test"), "Hello World!")
    system.eventStream.publish(tweet)

    mockSink.request(1)
    mockSink.expectNext()
    mockSink.expectNoMsg(noMessageTimeout)
  }

  "The flow for tweets with hashtag" should "only forward tweets with matching hashtag" in {
    val hashtag = "test-ok"
    val sut = HashtagFlow(hashtag).websocketFlow.runWith(TestSource.probe[Message], TestSink.probe[Message])
    val (_, mockSink) = sut

    val tweet = TimelineActor.Tweet(User("test"), s"Hello World! #${hashtag}")
    system.eventStream.publish(tweet)

    mockSink.request(1)
    mockSink.expectNext()
    mockSink.expectNoMsg(noMessageTimeout)
  }

  it should " not forward tweets without matching hashtag" in {
    val sut = HashtagFlow("test-ok").websocketFlow.runWith(TestSource.probe[Message], TestSink.probe[Message])
    val (_, mockSink) = sut

    val tweet = TimelineActor.Tweet(User("test"), "Hello World! #otherhashtag")
    system.eventStream.publish(tweet)

    mockSink.request(1)
    mockSink.expectNoMsg(noMessageTimeout)
  }

}
