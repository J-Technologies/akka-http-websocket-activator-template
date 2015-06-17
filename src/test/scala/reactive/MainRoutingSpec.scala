package reactive

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import akka.http.impl.engine.ws.InternalCustomHeader
import akka.http.impl.engine.ws.InternalCustomHeader
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.ws.UpgradeToWebsocket
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.headers.{ UpgradeProtocol, Upgrade }
import akka.pattern.ask
import akka.stream.ActorFlowMaterializer
import akka.stream.FlowMaterializer
import akka.util.Timeout
import akka.http.impl.engine.ws.InternalCustomHeader
import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model.headers.CustomHeader
import akka.http.impl.engine.ws.InternalCustomHeader

class MainRoutingSpec extends FlatSpec with Matchers with ScalatestRouteTest {
  "Main" should "respond to 'post' on /post" in {
    implicit val timeout = Timeout(1000 millis)
    implicit val materializer = ActorFlowMaterializer()

    Get("/post") ~> Main.mainFlow ~> check {
      status shouldBe OK
    }
  }

  it should "handle websocket requests for tweets" in {
    implicit val timeout = Timeout(1000 millis)
    implicit val materializer = ActorFlowMaterializer()
    Get("/") ~> Upgrade(List(UpgradeProtocol("websocket"))) ~> emulateHttpCore ~> Main.mainFlow ~> check {
      status shouldEqual StatusCodes.SwitchingProtocols
    }
  }
  
  it should "handle websocket requests for hashtags" in {
    implicit val timeout = Timeout(1000 millis)
    implicit val materializer = ActorFlowMaterializer()
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
      override def name = ""
      override def value = "UpgradeToWebsocketMock"

      override def handleMessages(handlerFlow: Flow[Message, Message, Any], subprotocol: Option[String])(implicit mat: FlowMaterializer): HttpResponse =
        HttpResponse(StatusCodes.SwitchingProtocols)
    }
}