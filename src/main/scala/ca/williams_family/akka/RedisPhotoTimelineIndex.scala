package ca.williams_family
package akka

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm._
import se.scalablesolutions.akka.config.ScalaConfig._
import se.scalablesolutions.akka.persistence.redis.RedisStorage

import net.liftweb.util.Helpers._

trait RedisPhotoTimelineIndexFactory {
  this: PhotoService =>
  val timelineIndex: ActorRef = this.self.spawnLink[RedisPhotoTimelineIndex]
}

class RedisPhotoTimelineIndex extends PhotoTimelineIndex with RedisHelpers {
  import PhotoTimelineTypes._

  self.lifeCycle = Some(LifeCycle(Permanent))

  val index = Ref(new Col)

  val keys = RedisStorage.newMap("photoTimelineIndex")

  Transaction.Global.atomic { for ((v,k) <- keys) setIndex(k,v) }

  def get(year: Option[Int], month: Option[Int], day: Option[Int]): PhotoTimeline =
    PhotoTimeline(((year, month, day) match {
      case (Some(y), Some(m), Some(d)) => index.get.map(_.range((y,m,d),(y,m,d+1)))
      case (Some(y), Some(m), None) => index.get.map(_.range((y,m,1),(y,m+1,1)))
      case (Some(y), None, None) => index.get.map(_.range((y,1,1),(y+1,1,1)))
      case (None, None, None) => index.get
      case _ => None
    }).getOrElse(new Col))


  def set(k: K, v: V): Unit = {
    keys.put(v, k)
    setIndex(k, v)
  }

  def setIndex(k: K, v: V): Unit = index.alter(i => i + ((k, (i.getOrElse(k, new VSet) + v))))

  implicit def keyToBytes(k: K): Array[Byte] = k._1+"-"+k._2+"-"+k._3
  
  implicit def bytesToKey(k: Array[Byte]): K = {
    val List(y: Int,m: Int,d: Int) = asString(k).split("-").toList.map(asInt).map(_.getOrElse(0))
    (y,m,d)
  }

}
