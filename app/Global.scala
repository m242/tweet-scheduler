import java.util.Date
import play.api.cache.Cache
import play.api.libs.concurrent.Akka
import play.api.{Logger, GlobalSettings}
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results._
import concurrent.duration._
import play.api.Play.current
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import services.TwitterCron

/**
 * Created with IntelliJ IDEA.
 * User: markbe
 * Date: 7/31/13
 * Time: 10:41 AM
 */

object Global extends GlobalSettings {

  private val CacheKey = "tweeting"

  override def onStart(app: play.api.Application) {
    import ExecutionContext.Implicits.global
    // Job to check for tweets once per minute, only if not currently running
    Akka.system.scheduler.schedule(10 seconds, 1 minute) {
      cron(app)
    }
  }

  override def onHandlerNotFound(request: RequestHeader): Result = {
    NotFound(views.html.notfound())
  }

  override def onError(request: RequestHeader, e: Throwable): Result = {
    InternalServerError(views.html.error(e))
  }

  def cron(app: play.api.Application) {
    Cache.get(CacheKey)(app).fold({
      Logger debug "Checking for tweets: " + new Date().getTime
      Cache.set(CacheKey, true)(app)
      try {
        TwitterCron()
      } catch {
        case e: Exception => Logger error "Exception in Tweet job: " + e.toString
      } finally {
        Cache.remove(CacheKey)(app)
      }
    })(v => {})
  }

}
