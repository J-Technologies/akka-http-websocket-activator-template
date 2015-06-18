package reactive.tweets.marshalling

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import reactive.tweets.domain.{Tweet, User}
import spray.json._

trait TweetJsonProtocol extends DefaultJsonProtocol {
   implicit val userFormat = jsonFormat1(User.apply)
   implicit val tweetFormat = jsonFormat2(Tweet.apply)

   implicit val unitMarshaller: ToEntityMarshaller[Tweet] = SprayJsonSupport.sprayJsonMarshaller[Tweet]
   implicit val unitListMarshaller: ToEntityMarshaller[List[Tweet]] = SprayJsonSupport.sprayJsonMarshaller[List[Tweet]]

   def toMessage(tweet: Tweet): Message = TextMessage.Strict(tweet.toJson.compactPrint)
 }
