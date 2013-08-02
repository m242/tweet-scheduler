package models

import org.ektorp.support.CouchDbDocument
import org.codehaus.jackson.annotate.JsonProperty
import services.CouchDb
import play.api.libs.json._
import org.apache.commons.lang3.StringEscapeUtils
import java.util.Date
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber
import scala.beans.BeanProperty

/**
 * Created with IntelliJ IDEA.
 * User: markbe
 * Date: 7/31/13
 * Time: 2:16 PM
 */

class Tweet extends CouchDbDocument {
  @JsonProperty @BeanProperty var userid: String = null
  @JsonProperty @BeanProperty var screenName: String = null
  @JsonProperty @BeanProperty var tweet: String = null
  @JsonProperty @BeanProperty var timestamp: Double = 0
  @JsonProperty @BeanProperty var token: String = null
  @JsonProperty @BeanProperty var secret: String = null
  @JsonProperty @BeanProperty var posted: Boolean = false
  @JsonProperty("_deleted_conflicts") @BeanProperty var deleted_conflicts: java.util.List[String] = null
}


object Tweet extends CouchDb {

  def apply(userid: String, screen_name: String, body: String, timestamp: Double, token: String, secret: String) = {
    val t = new Tweet()
    t.userid = userid
    t.screenName = screen_name
    t.timestamp = timestamp
    t.tweet = body
    t.token = token
    t.secret = secret
    t
  }

  def apply(id: String) = db.get[Tweet](classOf[Tweet], id)

}


object TweetListWrites extends Writes[List[Tweet]] {

  def writes(t: List[Tweet]) = JsArray(t.map(m => Json.toJson(m)(TweetWrites)))

}


object TweetWrites extends Writes[Tweet] {

  def writes(t: Tweet) = JsObject(Seq(
    "_id" -> JsString(t.getId),
    "screen_name" -> JsString(StringEscapeUtils.escapeHtml4(t.screenName)),
    "tweet" -> JsString(StringEscapeUtils.escapeHtml4(t.tweet)),
    "millis" -> JsNumber(t.timestamp - new Date().getTime.toDouble),
    "timestamp" -> JsNumber(t.timestamp)
  ))

}
