package reactive

import scala.reflect.ClassTag

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers

import akka.actor.ActorSystem
import akka.actor.Status
import akka.testkit.DefaultTimeout
import akka.testkit.ImplicitSender
import akka.testkit.TestKit

class ActorSpec extends TestKit(ActorSpec.actorSystem())
  with DefaultTimeout with ImplicitSender
  with FlatSpecLike with Matchers with BeforeAndAfterAll {
  override protected def afterAll() = shutdown()

  def expectFailure[A <: Exception: ClassTag] = {
    expectMsgPF() {
      case Status.Failure(e: A) => true
    }
  }
}

object ActorSpec {
  def actorSystem() = ActorSystem("TestKitActorSystem")
}

