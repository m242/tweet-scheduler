package services

import org.ektorp.impl.StdCouchDbInstance
import org.ektorp.http.StdHttpClient
import play.api.Play
import play.api.Play.current
import scala.collection.JavaConversions
import models.Tweet
import org.ektorp.ViewQuery
import java.util.Date


/**
 * Created with IntelliJ IDEA.
 * User: markbe
 * Date: 7/31/13
 * Time: 2:22 PM
 */


trait CouchDb {

  private val CouchHostname = Play.configuration.getString("couchdb.hostname").getOrElse("localhost")
  private val CouchPort = Play.configuration.getInt("couchdb.port").getOrElse(5984)
  private val CouchDatabase = Play.configuration.getString("couchdb.database").getOrElse("scheduledtweets")

  def db = new StdCouchDbInstance(
    new StdHttpClient.Builder().url("http://" + CouchHostname + ":" + CouchPort + "/").socketTimeout(30000).build()
  ).createConnector(CouchDatabase, true)

  def scheduledTweets(): List[Tweet] = JavaConversions.asScalaBuffer[Tweet](
    db.queryView(
      new ViewQuery().designDocId("_design/tweets").viewName("scheduled").startKey(0).endKey(new Date().getTime),
        classOf[Tweet]
    )
  ).toList

  def scheduledTweetsByUser(userid: String): List[Tweet] = JavaConversions.asScalaBuffer[Tweet](
    db.queryView(
      new ViewQuery().designDocId("_design/tweets").viewName("scheduledByUser").key(userid), classOf[Tweet]
    )
  ).toList.sortBy(_.timestamp)

}
