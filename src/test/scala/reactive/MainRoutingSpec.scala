package reactive

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.testkit.ScalatestRouteTest
import reactive.receive.TimelineActor
import reactive.receive.TimelineActorManager
import reactive.receive.User
import akka.pattern.ask
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import akka.util.Timeout
import akka.stream.ActorFlowMaterializer
import scala.concurrent.Await

class MainRoutingSpec extends FlatSpec with Matchers with ScalatestRouteTest {
  "Main" should "respond with index on /" in {
    implicit val timeout = Timeout(1000 millis)
    implicit val materializer = ActorFlowMaterializer()

    Get("/post") ~> Main.mainFlow ~> check {
      status shouldBe OK
    }
  }
}