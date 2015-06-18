package reactive

import akka.http.scaladsl.model.StatusCodes.NoContent
import akka.http.scaladsl.model.headers.{CustomHeader, Upgrade, UpgradeProtocol}
import akka.http.scaladsl.model.ws.{Message, UpgradeToWebsocket}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import org.scalatest.{FlatSpec, Matchers}
import reactive.tweets.domain.{Tweet, User}
import reactive.tweets.marshalling.TweetJsonProtocol

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class MainRoutingSpec extends FlatSpec with Matchers with ScalatestRouteTest with TweetJsonProtocol {
  implicit val timeout = Timeout(1000 millis)

  "Main" should "allow to post a tweet for a user" in {
    Post("/", Tweet(User("Test"), "Some tweet")) ~> Main.mainFlow ~> check {
      status shouldBe NoContent
    }
  }

  it should "handle websocket requests for tweets" in {
    Get("/all") ~> Upgrade(List(UpgradeProtocol("websocket"))) ~> emulateHttpCore ~> Main.mainFlow ~> check {
      status shouldEqual StatusCodes.SwitchingProtocols
    }
  }
  
  it should "handle websocket requests for hashtags" in {
    Get("/hashtag/test") ~> Upgrade(List(UpgradeProtocol("websocket"))) ~> emulateHttpCore ~> Main.mainFlow ~> check {
      status shouldEqual StatusCodes.SwitchingProtocols
    }
  }

  /** Only checks for upgrade header and then adds UpgradeToWebsocket mock header */
  private def emulateHttpCore(req: HttpRequest): HttpRequest =
    req.header[Upgrade] match {
      case Some(upgrade) if upgrade.hasWebsocket ⇒ req.copy(headers = req.headers :+ upgradeToWebsocketHeaderMock)
      case _                                     ⇒ req
    }

  private def upgradeToWebsocketHeaderMock: UpgradeToWebsocket =
    new CustomHeader() with UpgradeToWebsocket {
      override def requestedProtocols = Nil
      override def name = "dummy"
      override def value = "dummy"

      override def handleMessages(handlerFlow: Flow[Message, Message, Any], subprotocol: Option[String])(implicit mat: FlowMaterializer): HttpResponse =
        HttpResponse(StatusCodes.SwitchingProtocols)
    }
}