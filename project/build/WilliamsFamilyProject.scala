import sbt._
import FileUtilities._

class WilliamsFamilyProject(info: ProjectInfo) extends DefaultWebProject(info)
{
  override def compileOptions = Unchecked :: super.compileOptions.toList

  override def ivyXML=
    <dependencies>
      <dependency org="net.liftweb" name="lift-webkit" rev="2.0-scala280-SNAPSHOT"><exclude module="log4j"/><exclude module="slf4j-log4j12"/></dependency>
      <dependency org="net.liftweb" name="lift-util" rev="2.0-scala280-SNAPSHOT"><exclude module="log4j"/><exclude module="slf4j-log4j12"/></dependency>
      <dependency org="net.liftweb" name="lift-record" rev="2.0-scala280-SNAPSHOT"><exclude module="log4j"/><exclude module="slf4j-log4j12"/></dependency>
      <dependency org="net.liftweb" name="lift-json" rev="2.0-scala280-SNAPSHOT"><exclude module="log4j"/><exclude module="slf4j-log4j12"/></dependency>
    </dependencies>

  //val liftWebkit = "net.liftweb" % "lift-webkit" % "2.0-scala280-SNAPSHOT"
  //val liftRecord = "net.liftweb" % "lift-record" % "2.0-scala280-SNAPSHOT"
  //val liftJson = "net.liftweb" % "lift-json" % "2.0-scala280-SNAPSHOT"
  //val dispatchHttp = "net.databinder" %% "dispatch-http" % "0.7.0"

  //val flickr = "net.fyrie" %% "flickr-api" % "1.0-SNAPSHOT"

  val akkaRedis = "se.scalablesolutions.akka" %% "akka-persistence-redis"  % "0.8.1" % "compile"

  val slf4japi = "org.slf4j" % "slf4j-api" % "1.5.11"
  val jcloverslf4j = "org.slf4j" % "jcl-over-slf4j" % "1.5.11"
  val log4joverslf4j = "org.slf4j" % "log4j-over-slf4j" % "1.5.11"
  val logback = "ch.qos.logback" % "logback-classic" % "0.9.18"
  
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.4" % "test->default"
  val scalacheck = "org.scala-tools.testing" %% "scalacheck" % "1.7-SNAPSHOT" % "test->default"
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.23" % "test->default"
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided->default"

  override def jettyPort = 8090

  // For JRebel
  override def scanDirectories = Nil

  override def repositories = Set(
    "jBoss" at "http://repository.jboss.org/maven2",
    "Multiverse Releases" at "http://multiverse.googlecode.com/svn/maven-repository/releases/",
    "GuiceyFruit" at "http://guiceyfruit.googlecode.com/svn/repo/releases/",
    "DataBinder" at "http://databinder.net/repo",
    "Configgy" at "http://www.lag.net/repo",
    "Akka Maven Repository" at "http://scalablesolutions.se/akka/repository",
    "Java.Net" at "http://download.java.net/maven/2",
    ScalaToolsSnapshots)

  //val fyrieReleases = "Fyrie.net Releases" at "http://repo.fyrie.net/releases"
  //val fyrieSnapshots = "Fyrie.net Snapshots" at "http://repo.fyrie.net/snapshots"
  //val databinder = "Databinder" at "http://databinder.net/repo"
}

