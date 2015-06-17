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
import akka.pattern.ask
import akka.stream.ActorFlowMaterializer
import akka.stream.FlowMaterializer
import akka.util.Timeout

class MainRoutingSpec extends FlatSpec with Matchers with ScalatestRouteTest {
  "Main" should "respond with index on /" in {
    implicit val timeout = Timeout(1000 millis)
    implicit val materializer = ActorFlowMaterializer()

    Get("/post") ~> Main.mainFlow ~> check {
      status shouldBe OK
    }
  }

  "the handleWebsocketMessages directive" should "handle websocket requests" in {
    implicit val timeout = Timeout(1000 millis)
    implicit val materializer = ActorFlowMaterializer()
    Get("/") ~> Main.mainFlow ~> check {
      status shouldEqual StatusCodes.SwitchingProtocols
    }
  }
}