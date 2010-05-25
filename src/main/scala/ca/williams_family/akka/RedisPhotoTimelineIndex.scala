package ca.williams_family
package akka

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.config.ScalaConfig._

import net.liftweb.util.Helpers._

import com.redis._

trait RedisPhotoTimelineIndexFactory {
  this: PhotoService =>
  val timelineIndex: ActorRef = this.self.spawnLink[RedisPhotoTimelineIndex]
}

class RedisPhotoTimelineIndex extends PhotoTimelineIndex with RedisHelpers {
  import PhotoTimelineTypes._

  self.lifeCycle = Some(LifeCycle(Permanent))

  val r = new RedisClient("localhost", 6379)

  val ns = "photoTimelineIndex"

  var index = new Col

  for {
    keys <- r.keys(ns+":*")
    Some(key) <- keys
    value <- r.get(key)
  } setIndex(value, key.split(":")(1)) // FIXME: Make this less fragile

  def get(year: Option[Int], month: Option[Int], day: Option[Int]): PhotoTimeline =
    PhotoTimeline((year, month, day) match {
      case (Some(y), Some(m), Some(d)) => index.range((y,m,d),(y,m,d+1))
      case (Some(y), Some(m), None) => index.range((y,m,1),(y,m+1,1))
      case (Some(y), None, None) => index.range((y,1,1),(y+1,1,1))
      case (None, None, None) => index
      case _ => new Col
    })


  def set(k: K, v: V): Unit = {
    r.set(ns+":"+v, k)
    setIndex(k, v)
  }

  def setIndex(k: K, v: V): Unit = index += (k -> (index.getOrElse(k, new VSet) + v))

  implicit def keyToString(k: K): String = k._1+"-"+k._2+"-"+k._3
  
  implicit def stringToKey(k: String): K = {
    val List(y: Int,m: Int,d: Int) = k.split("-").toList.map(asInt).map(_.getOrElse(0))
    (y,m,d)
  }

}
