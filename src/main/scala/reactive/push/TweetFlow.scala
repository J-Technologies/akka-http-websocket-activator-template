package reactive.push

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.unmarshalling._
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import reactive.receive.TimelineActor.Tweet
import reactive.receive.User
import spray.json._

trait TweetJsonProtocol extends DefaultJsonProtocol {
  val tweetSource: Source[Tweet, ActorRef] = Source.actorPublisher[Tweet](TimelineSourceActor.props)
  def toMessage(tweet: Tweet): Message = TextMessage.Strict(tweet.toJson.compactPrint)

  implicit val userFormat = jsonFormat1(User.apply)
  implicit val tweetFormat = jsonFormat2(Tweet.apply)
  implicit val tweetMarshaller: ToEntityMarshaller[Tweet] = SprayJsonSupport.sprayJsonMarshaller[Tweet]
  implicit def tweetUnmarshaller(implicit materializer: FlowMaterializer): FromEntityUnmarshaller[Tweet] =
    SprayJsonSupport.sprayJsonUnmarshaller[Tweet]
}

object TweetFlow extends TweetJsonProtocol {

  def ofAll() = toWebsocketFlow(tweetSource map toMessage)

  private def toWebsocketFlow[Mat](source: Source[Message, Mat]) = Flow(Sink.ignore, source)(Keep.left) { implicit b =>
    (sink, source) => (sink.inlet, source.outlet)
  }
}