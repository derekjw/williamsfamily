import sbt._
import FileUtilities._

class WilliamsFamilyProject(info: ProjectInfo) extends DefaultWebProject(info)
{
  override def compileOptions = Optimize :: Unchecked :: super.compileOptions.toList

  override def ivyXML=
    <dependencies>
      <dependency org="net.liftweb" name="lift-webkit" rev="2.0-scala280-SNAPSHOT"><exclude module="log4j"/><exclude module="slf4j-log4j12"/></dependency>
      <dependency org="net.liftweb" name="lift-util" rev="2.0-scala280-SNAPSHOT"><exclude module="log4j"/><exclude module="slf4j-log4j12"/></dependency>
      <dependency org="net.liftweb" name="lift-json" rev="2.0-scala280-SNAPSHOT"><exclude module="log4j"/><exclude module="slf4j-log4j12"/></dependency>
    </dependencies>

  val liftFacebook = "net.liftweb" % "lift-facebook" % "2.0-scala280-SNAPSHOT"

  val akkaCore = "se.scalablesolutions.akka" %% "akka-core"  % "0.9-SNAPSHOT" % "compile"
  val redis = "com.redis" %% "redisclient" % "2.0-SNAPSHOT" % "compile"

  val apacheMath = "org.apache.commons" % "commons-math" % "2.1"

  val slf4japi = "org.slf4j" % "slf4j-api" % "1.5.11"
  val jcloverslf4j = "org.slf4j" % "jcl-over-slf4j" % "1.5.11"
  val log4joverslf4j = "org.slf4j" % "log4j-over-slf4j" % "1.5.11"

  val logback = "ch.qos.logback" % "logback-classic" % "0.9.18"
  
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.5-SNAPSHOT" % "test->default"
  val scalacheck = "org.scala-tools.testing" %% "scalacheck" % "1.7" % "test->default"
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.23" % "test->default"
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided->default"

  override def jettyPort = 8090

  override def repositories = Set(
    "Fyrie Snapshots" at "http://repo.fyrie.net/snapshots",
    "Fyrie Akka Deps" at "http://repo.fyrie.net/akka-deps",
    "Codehaus" at "http://repository.codehaus.org",
    "Codehaus Snapshots" at "http://snapshots.repository.codehaus.org",
    "jBoss" at "http://repository.jboss.org/maven2",
    "Multiverse Releases" at "http://multiverse.googlecode.com/svn/maven-repository/releases/",
    "GuiceyFruit" at "http://guiceyfruit.googlecode.com/svn/repo/releases/",
    "DataBinder" at "http://databinder.net/repo",
    "Configgy" at "http://www.lag.net/repo",
    "Akka Maven Repository" at "http://scalablesolutions.se/akka/repository",
    "Java.Net" at "http://download.java.net/maven/2",
    ScalaToolsSnapshots)

}

