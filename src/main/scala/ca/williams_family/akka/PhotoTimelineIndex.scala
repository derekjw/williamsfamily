package ca.williams_family
package akka

import collection.immutable.{TreeMap,TreeSet,SortedMap}

import model._

object PhotoTimelineTypes {
  type K = (Int, Int, Int)
  type V = String
  type VSet = TreeSet[V]
  type Col = TreeMap[K,VSet]
}

trait PhotoTimelineIndex extends PhotoIndex {
  import PhotoTimelineTypes._

  def get(year: Option[Int], month: Option[Int], day: Option[Int]): PhotoTimeline

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

case class PhotoTimeline(value: PhotoTimelineTypes.Col) extends SortedMap[PhotoTimelineTypes.K, PhotoTimelineTypes.VSet] {
  import PhotoTimelineTypes._
  type ColWrapper = SortedMap[PhotoTimelineTypes.K, PhotoTimelineTypes.VSet]

  def -(key: K) = value - key

  def rangeImpl(from: Option[K], until: Option[K]) = value.rangeImpl(from, until)

  def ordering = value.ordering

  def iterator = value.iterator

  def get(key: K) = value.get(key)

  def sizeAll = valuesIterator.foldLeft(0)(_ + _.size)

  def valuesAll = valuesIterator.map(_.iterator).flatten

  def exists(value: V): Boolean = exists(_._2(value))

  def apply(value: V): Boolean = exists(value)

  def groupByYMD = foldLeft(TreeMap[Int, TreeMap[Int, TreeMap[Int, VSet]]]()){
    case (cy, ((y,m,d),v)) => {
      val cm = cy.get(y).getOrElse(TreeMap[Int, TreeMap[Int, VSet]]())
      val cd = cm.get(m).getOrElse(TreeMap[Int, VSet]())
      cy + (y -> (cm + (m -> (cd + (d -> v)))))
    }


  }

}
