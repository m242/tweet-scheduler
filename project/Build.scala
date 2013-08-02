import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "tweet-scheduler"
  val appVersion      = "1.0.1"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "org.ektorp" % "org.ektorp" % "1.4.0",
    "org.twitter4j" % "twitter4j-core" % "3.0.3"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    scalacOptions += "-feature"
  )

}
