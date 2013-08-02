package controllers

import play.api.mvc._
import services.{CouchDb, TwitterUser, Twitter}
import twitter4j.auth.RequestToken
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.data.Form
import play.api.data.Forms._
import java.util.Date
import models.{TweetListWrites, Tweet}
import play.api.Logger
import play.api.libs.json.Json._

object Application extends Controller with Twitter with CouchDb {

  def index = Action { implicit request =>
    twitterUser match {
      case Some(t) => Ok(views.html.form(t))
      case None => Ok(views.html.index())
    }
  }


  def auth = Action { implicit request =>
    val future = Future {
      TwitterService.getInstance().getOAuthRequestToken("http://" + request.host + routes.Application.callback()) match {
        case token: RequestToken => Redirect(token.getAuthenticationURL).withSession(
          "token" -> token.getToken,
          "secret" -> token.getTokenSecret
        )
        case _ => throw new Exception("Invalid Twitter auth.")
      }
    }
    Async { future }
  }


  def callback = Action { implicit request =>
    request.queryString("oauth_verifier").map { verifier =>
      requestToken match {
        case Some(token) => {
          val future = Future {
            val accessToken = TwitterService.getInstance().getOAuthAccessToken(token, verifier)
            Redirect(routes.Application.index()).withSession(
              "token" -> accessToken.getToken,
              "secret" -> accessToken.getTokenSecret,
              "screen_name" -> accessToken.getScreenName,
              "userid" -> accessToken.getUserId.toString,
              "profile_img" -> TwitterService.getInstance(accessToken).showUser(accessToken.getUserId).getProfileImageURLHttps
            )
          }
          Async { future }
        }
        case None => throw new Exception("Incorrect token.")
      }
    }.head
  }


  def signout = Action { implicit request => Ok(views.html.signout()).withNewSession }


  def scheduled = TwitterAction { user => implicit request =>
    Ok(toJson(scheduledTweetsByUser(user.userid))(TweetListWrites))
  }


  def post = TwitterAction { user => implicit request =>
    requestToken match {
      case Some(token) => {
        val tweetForm = Form(tuple(
          "tweet" -> text(minLength = 1, maxLength = 140),
          "millis" -> number(min = 0)
        ))
        tweetForm.bindFromRequest.fold(
          formWithErrors => BadRequest(toJson(formWithErrors.errorsAsJson)),
          validForm => {
            val d = new Date(new Date().getTime + validForm._2)
            Logger info "Scheduled tweet for " + user.screen_name + " at " + d.getTime
            db.create(Tweet(user.userid, user.screen_name, validForm._1, d.getTime.toDouble, token.getToken, token.getTokenSecret))
            Ok(toJson(Map("screen_name" -> toJson(user.screen_name),
              "tweet" -> toJson(validForm._1),
              "timestamp" -> toJson(d.getTime)
            )))
          }
        )
      }
      case None => BadRequest(toJson(Map("error" -> "No Twitter token.")))
    }
  }


  def delete(id: String) = TwitterAction { user => implicit request =>
    val tweet = Tweet(id)
    tweet.userid == user.userid match {
      case true => {
        Logger info "Deleted tweet for " + user.screen_name + ": " + id
        db.delete(tweet)
        Ok(toJson(Map("id" -> toJson(id))))
      }
      case false => BadRequest(toJson(Map("error" -> "Invalid id.")))
    }
  }


  def exception = Action { implicit request => throw new Exception("Error.") }


  def TwitterAction(f: => TwitterUser => Request[AnyContent] => Result) = Action { implicit request =>
    twitterUser match {
      case Some(t) => f(t)(request)
      case None => BadRequest(toJson(Map("error" -> "Invalid Twitter User")))
    }
  }

}