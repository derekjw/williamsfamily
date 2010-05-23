package ca.williams_family
package akka

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm._
import Transaction.{Global,Local}
import se.scalablesolutions.akka.config.ScalaConfig._

trait InMemoryPhotoTimelineIndexFactory {
  this: PhotoService =>
  val timelineIndex: ActorRef = this.self.spawnLink[InMemoryPhotoTimelineIndex]
}

class InMemoryPhotoTimelineIndex extends PhotoTimelineIndex {
  import PhotoTimelineTypes._

  self.lifeCycle = Some(LifeCycle(Permanent))

  private val index = TransactionalState.newRef(new Col)

  private val keys = TransactionalState.newMap[String, String]

  def get(year: Option[Int], month: Option[Int], day: Option[Int]): PhotoTimeline = Local.atomic {
    PhotoTimeline(((year, month, day) match {
      case (Some(y), Some(m), Some(d)) => index.get.map(_.range((y,m,d),(y,m,d+1)))
      case (Some(y), Some(m), None) => index.get.map(_.range((y,m,1),(y,m+1,1)))
      case (Some(y), None, None) => index.get.map(_.range((y,1,1),(y+1,1,1)))
      case (None, None, None) => index.get
      case _ => None
    }).getOrElse(new Col))
  }

  def set(k: K, v: V): Unit = Global.atomic {
    index.alter(i => i + (k -> (i.getOrElse(k, new VSet) + v)))
  }

}


