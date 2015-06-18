package reactive.tweets.domain

trait WithUser {
  def user: User
}
case class Tweet(user: User, text: String) extends WithUser

case class User(name: String)
