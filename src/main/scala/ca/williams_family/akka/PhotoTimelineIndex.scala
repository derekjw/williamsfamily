package ca.williams_family
package akka

import collection.immutable.{TreeMap,TreeSet}

import model._

object PhotoTimelineTypes {
  type K = (Int, Int, Int)
  type V = String
  type VSet = TreeSet[V]
  type Col = TreeMap[K,VSet]
}

trait PhotoTimelineIndex extends PhotoIndex {
  import PhotoTimelineTypes._

  def get(year: Option[Int], month: Option[Int], day: Option[Int]): Col

  def set(k: K, v: V): Unit

  def receive = {
    case GetPhotoTimeline(y,m,d) =>
      reply(get(y,m,d))
    
    case SetPhoto(DateAndId(k,v)) =>
      set(k,v)
      reply_?(true)

    case SetPhoto(_) =>
      reply_?(false)
  }

  object DateAndId {
    def unapply(photo: Photo): Option[((Int, Int, Int), String)] = photo.createDate match {
      case y :: m :: d :: _ => Some(((y,m,d),photo.id))
      case _ => None
    }
  }

}
