package reactive.tweets.domain

case class Tweet(user: User, text: String)

case class User(name: String)
