package reactive.tweets.marshalling

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import reactive.tweets.domain.{Tweet, User}
import spray.json._

trait TweetJsonProtocol extends DefaultJsonProtocol {
   implicit val userFormat = jsonFormat1(User.apply)
   implicit val tweetFormat = jsonFormat2(Tweet.apply)

   implicit val tweetMarshaller: ToEntityMarshaller[Tweet] = SprayJsonSupport.sprayJsonMarshaller[Tweet]
   implicit val tweetListMarshaller: ToEntityMarshaller[List[Tweet]] = SprayJsonSupport.sprayJsonMarshaller[List[Tweet]]
   implicit def tweetUnmarshaller(implicit materializer: Materializer): FromEntityUnmarshaller[Tweet] =
      SprayJsonSupport.sprayJsonUnmarshaller[Tweet]

   def toMessage(tweet: Tweet): Message = TextMessage.Strict(tweet.toJson.compactPrint)
 }
