package services

import play.api.{Logger, Play}
import twitter4j.conf.ConfigurationBuilder
import twitter4j.TwitterFactory
import play.api.Play.current
import twitter4j.auth.{AccessToken, RequestToken}
import play.api.mvc.RequestHeader
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, Future}
import models.Tweet
import scala.language.postfixOps
import concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created with IntelliJ IDEA.
 * User: markbe
 * Date: 7/31/13
 * Time: 11:10 AM
 */

trait Twitter {

  private val TwitterKey = Play.configuration.getString("twitter.key").getOrElse("x")
  private val TwitterSecret = Play.configuration.getString("twitter.secret").getOrElse("y")

  val TwitterService = {
    val c = new ConfigurationBuilder()
    c.setOAuthConsumerKey(TwitterKey)
    c.setOAuthConsumerSecret(TwitterSecret)
    c.setUseSSL(true)
    c.setDebugEnabled(true)
    new TwitterFactory(c.build())
  }

  def requestToken(implicit request: RequestHeader) = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      new RequestToken(token, secret)
    }
  }

  def twitterUser(implicit request: RequestHeader) = {
    for {
      screen_name <- request.session.get("screen_name")
      profile_img <- request.session.get("profile_img")
      userid <- request.session.get("userid")
    } yield {
      new TwitterUser(userid, screen_name, profile_img)
    }
  }

  def sendTweet(tweet: Tweet): Future[Boolean] = Future {
    TwitterService.getInstance(new AccessToken(tweet.token, tweet.secret)).updateStatus(tweet.tweet).getText.length > 0
  }

}


case class TwitterUser(userid: String, screen_name: String, profile_img: String)


object TwitterCron extends Twitter with CouchDb {

  def apply() {
    val tweets = scheduledTweets()
    val futures = ArrayBuffer[Future[Any]]()
    Logger debug "Tweets to be posted: " + tweets.length
    tweets.foreach(tweet => {
      try {
        Logger info "Posting tweet for " + tweet.screenName + ": " + tweet.getId
        val future = sendTweet(tweet)
        future onSuccess {
          case true => {
            tweet.posted = true
            db.update(tweet)
          }
          case false => {
            throw new Exception("Status did not post.")
          }
        }
        future onFailure { case e => throw e }
        futures.append(future)
      } catch {
        case ex: Exception => Logger error "Exception in posting tweet: " + tweet.getId + " : " + ex.toString
      }
    })
    Await.result(Future.sequence(futures), 10 minutes)
  }

}