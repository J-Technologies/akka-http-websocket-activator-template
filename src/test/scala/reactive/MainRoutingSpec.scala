package reactive

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers.CustomHeader
import akka.http.scaladsl.model.headers.Upgrade
import akka.http.scaladsl.model.headers.UpgradeProtocol
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.ws.UpgradeToWebsocket
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorFlowMaterializer
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout

class MainRoutingSpec extends FlatSpec with Matchers with ScalatestRouteTest {
  implicit val timeout = Timeout(1000 millis)

  "Main" should "respond to 'post' on /post" in {
    Get("/post") ~> Main.mainFlow ~> check {
      status shouldBe OK
    }
  }

  it should "handle websocket requests for tweets" in {
    Get("/") ~> Upgrade(List(UpgradeProtocol("websocket"))) ~> emulateHttpCore ~> Main.mainFlow ~> check {
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