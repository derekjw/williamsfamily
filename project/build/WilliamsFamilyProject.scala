import sbt._
import FileUtilities._

class WilliamsFamilyProject(info: ProjectInfo) extends DefaultWebProject(info) with AkkaBaseProject
{
  override def compileOptions = Optimize :: Unchecked :: super.compileOptions.toList

  val liftWebkit = "net.liftweb" % "lift-webkit_2.8.0" % "2.2-M1"
  val liftRecord = "net.liftweb" % "lift-record_2.8.0" % "2.2-M1"
  val fyrieRedis = "net.fyrie" %% "fyrie-redis" % "0.1-SNAPSHOT"

  val ratio = "net.fyrie" % "ratio-datatype_2.8.0" % "1.0-SNAPSHOT" % "compile"

  val slf4japi = "org.slf4j" % "slf4j-api" % "1.6.1"
  val jcloverslf4j = "org.slf4j" % "jcl-over-slf4j" % "1.6.1"
  val log4joverslf4j = "org.slf4j" % "log4j-over-slf4j" % "1.6.1"

  val logback = "ch.qos.logback" % "logback-classic" % "0.9.26"
  
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test"
  val scalacheck = "org.scala-tools.testing" % "scalacheck_2.8.0" % "1.7" % "test"
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.23" % "test"
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"

  override def jettyPort = 8090

  val fyrieSnapshots          = "Fyrie Snapshots" at "http://repo.fyrie.net/snapshots"
  val scalaToolsSnapshots     = ScalaToolsSnapshots
}

