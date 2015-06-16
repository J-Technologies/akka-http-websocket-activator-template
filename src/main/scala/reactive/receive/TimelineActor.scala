package reactive.receive

import akka.actor._
import akka.event.LoggingReceive
import akka.persistence.PersistentActor
import reactive.receive.TimelineActor.Tweet

object TimelineActor {

  def props(user: User): Props = Props(new TimelineActor(user))

  case class Tweet(user: User, text: String)
}

class TimelineActor(val user: User) extends PersistentActor {

  override def persistenceId = user.name

  override def receiveCommand = {
    case tweet: Tweet =>
      persist(tweet) { event =>
        println("saved!!!!")
        sender() ! Status.Success
        context.system.eventStream.publish(tweet)
      }

    case msg => throw new UnsupportedOperationException(s"received unexpected message $msg from ${sender()}")
  }
  
  override def receiveRecover = {
    case tweet: Tweet => println(tweet)
    case _ =>
  }
}