package reactive.tweets.outgoing

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.ActorFlowMaterializer
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import reactive.ActorTestUtils
import reactive.tweets.domain.{Tweet, User}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class TweetFlowSpec extends ActorTestUtils with TweetFlow {
  implicit val materializer = ActorFlowMaterializer()
  private val noMessageTimeout = 100 millis

  "The flow for tweets" should "ignore incoming messages" in {
    val sut = tweetFlowOfAll.runWith(TestSource.probe[Message], TestSink.probe[Message])
    val (mockSource, mockSink) = sut

    mockSource.sendNext(TextMessage.Strict("Should be ignored"))

    mockSink.request(1)
    mockSink.expectNoMsg(noMessageTimeout)
  }

  it should "forward all tweets published to the event stream" in {
    val sut = tweetFlowOfAll.runWith(TestSource.probe[Message], TestSink.probe[Message])
    val (_, mockSink) = sut
    
    val tweet = Tweet(User("test"), "Hello World!")
    system.eventStream.publish(tweet)

    mockSink.request(1)
    mockSink.expectNext()
    mockSink.expectNoMsg(noMessageTimeout)
  }

  "The flow for tweets with user" should "only forward tweets with matching user name" in {
    val userName = "test"
    val sut = tweetFlowOfUser(userName).runWith(TestSource.probe[Message], TestSink.probe[Message])
    val (_, mockSink) = sut

    val tweet = Tweet(User(userName), s"Hello World!")
    system.eventStream.publish(tweet)

    mockSink.request(1)
    mockSink.expectNext()
    mockSink.expectNoMsg(noMessageTimeout)
  }

  it should " not forward tweets form users with a diffirent name" in {
    val sut = tweetFlowOfUser("diffirent").runWith(TestSource.probe[Message], TestSink.probe[Message])
    val (_, mockSink) = sut

    val tweet = Tweet(User("test"), "Hello World!")
    system.eventStream.publish(tweet)

    mockSink.request(1)
    mockSink.expectNoMsg(noMessageTimeout)
  }
  
  "The flow for tweets with hash tag" should "only forward tweets with matching hash tag" in {
	  val hashTag = "test-ok"
			  val sut = tweetFlowOfHashTag(hashTag).runWith(TestSource.probe[Message], TestSink.probe[Message])
			  val (_, mockSink) = sut
			  
			  val tweet = Tweet(User("test"), s"Hello World! #${hashTag}")
			  system.eventStream.publish(tweet)
			  
			  mockSink.request(1)
			  mockSink.expectNext()
			  mockSink.expectNoMsg(noMessageTimeout)
  }
  
  it should " not forward tweets without matching hash tag" in {
	  val sut = tweetFlowOfHashTag("test-ok").runWith(TestSource.probe[Message], TestSink.probe[Message])
			  val (_, mockSink) = sut
			  
			  val tweet = Tweet(User("test"), "Hello World! #otherhashtag")
			  system.eventStream.publish(tweet)
			  
			  mockSink.request(1)
			  mockSink.expectNoMsg(noMessageTimeout)
  }

}
