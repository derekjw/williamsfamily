package ca.williams_family
package akka

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.config.ScalaConfig._
import se.scalablesolutions.akka.stm.HashTrie

trait InMemoryPhotoTimelineIndexFactory {
  this: PhotoService =>
  val timelineIndex: ActorRef = this.self.spawnLink[InMemoryPhotoTimelineIndex]
}

class InMemoryPhotoTimelineIndex extends PhotoTimelineIndex {
  import PhotoTimelineTypes._

  self.lifeCycle = Some(LifeCycle(Permanent))

  var index = new Col

  var keys = new HashTrie[V, K]

  def get(year: Option[Int], month: Option[Int], day: Option[Int]): PhotoTimeline =
    PhotoTimeline((year, month, day) match {
      case (Some(y), Some(m), Some(d)) => index.range((y,m,d),(y,m,d+1))
      case (Some(y), Some(m), None) => index.range((y,m,1),(y,m+1,1))
      case (Some(y), None, None) => index.range((y,1,1),(y+1,1,1))
      case (None, None, None) => index
      case _ => new Col
    })

  def set(k: K, v: V): Unit = {
    keys += (v -> k)
    index += (k -> (index.getOrElse(k, new VSet) + v))
  }

}


