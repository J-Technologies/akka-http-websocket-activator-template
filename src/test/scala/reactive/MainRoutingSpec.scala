package reactive

import akka.http.scaladsl.model.StatusCodes.{ NoContent, OK, SwitchingProtocols }
import akka.http.scaladsl.model.headers.{ CustomHeader, Upgrade, UpgradeProtocol }
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.ws.{ Message, UpgradeToWebsocket }
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import org.scalatest.{ FlatSpec, Matchers }
import reactive.tweets.domain.{ Tweet, User }
import reactive.tweets.marshalling.TweetJsonProtocol
import scala.concurrent.duration.DurationInt
import akka.http.scaladsl.testkit.WSProbe
import reactive.tweets.incoming.TweetActorManager

class MainRoutingSpec extends FlatSpec with Matchers with ScalatestRouteTest with TweetJsonProtocol {
  implicit val timeout = Timeout(1000.millis)
  val tweetActorManager = system.actorOf(TweetActorManager.props)

  "Main" should "serve the index page on /" in {
    Get("/") ~> Main.mainFlow ~> check {
      status shouldBe OK
    }
  }
  
  it should "allow to post a tweet for a user" in {
    Post("/resources/tweets", Tweet(User("test"), "Some tweet")) ~> Main.mainFlow ~> check {
      status shouldBe NoContent
    }
  }

  it should "serve tweets of a user on /resources/tweets/users/test" in {
    Get("/resources/tweets/users/test") ~> Main.mainFlow ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      entityAs[String] should include regex ("Some tweet")
    }
  }
  
  it should "not handle websocket messages on /" in {
    val wsClient = WSProbe()

    WS("http://localhost/", wsClient.flow) ~> Main.mainFlow ~>
      check {
        isWebsocketUpgrade shouldEqual false
      }
  }
  
  it should "send tweets to the all websocket" in {
    val wsClient = WSProbe()

    WS("http://localhost/ws/tweets/all", wsClient.flow) ~> Main.mainFlow ~>
      check {
        isWebsocketUpgrade shouldEqual true

        tweetActorManager ! Tweet(User("test"), "Hello World!")
        wsClient.expectMessage("""{"user":{"name":"test"},"text":"Hello World!"}""")
      }
  }

  it should "send tweets to the stream of a user" in {
    val wsClient = WSProbe()

    WS("http://localhost/ws/tweets/users/test", wsClient.flow) ~> Main.mainFlow ~>
      check {
        isWebsocketUpgrade shouldEqual true

        tweetActorManager ! Tweet(User("test"), "Hello World!")
        wsClient.expectMessage("""{"user":{"name":"test"},"text":"Hello World!"}""")
      }
  }
  
  it should "not send tweets to the stream of a different user" in {
    val wsClient = WSProbe()

    WS("http://localhost/ws/tweets/users/test", wsClient.flow) ~> Main.mainFlow ~>
      check {
        isWebsocketUpgrade shouldEqual true

        tweetActorManager ! Tweet(User("test"), "Hello World!")
        wsClient.expectMessage("""{"user":{"name":"test"},"text":"Hello World!"}""")
       
        tweetActorManager ! Tweet(User("notest"), "Hello World!")
        wsClient.expectNoMessage()
      }
  }

  /**
   * TODO Make this test succeed (Part 2 of tutorial)
   */
   it should "send tweets to the stream of a hashtag" in {
    val wsClient = WSProbe()

    WS("http://localhost/ws/tweets/hashtag/test", wsClient.flow) ~> Main.mainFlow ~>
      check {
        isWebsocketUpgrade shouldEqual true

        tweetActorManager ! Tweet(User("tester"), "Hello World! #test")
        wsClient.expectMessage("""{"user":{"name":"tester"},"text":"Hello World! #test"}""")
      }
  }
}