package ca.williams_family
package akka

import net.liftweb.util.Helpers._

import collection.immutable.{TreeMap,TreeSet,SortedMap,SortedSet}

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
      self.reply(get(y,m,d))
    
    case SetPhoto(DateAndId(k,v)) =>
      set(k,v)

  }

  object DateAndId {
    def unapply(photo: Photo): Option[((Int, Int, Int), String)] = photo.createDate match {
      case y :: m :: d :: _ => Some(((y,m,d),photo.id))
      case _ => None
    }
  }

}

case class PhotoTimeline(treemap: PhotoTimelineTypes.Col)(implicit val ordering: Ordering[String]) extends SortedSet[PhotoTimelineTypes.V] {
  import PhotoTimelineTypes._

  def mkSet = logTime("Making new SortedSet from PhotoTimeline")(TreeSet[V]() ++ iterator)

  def rangeImpl(from: Option[V], until: Option[V]) = mkSet.rangeImpl(from, until)

  def -(elem: V) = mkSet - elem

  def +(elem: V) = mkSet + elem

  def contains(elem: V) = treemap.exists(_._2(elem))

  def iterator = treemap.valuesIterator.map(_.iterator).flatten

  override def size = treemap.valuesIterator.foldLeft(0)(_ + _.size)

  def groupByYMD = treemap.foldLeft(TreeMap[Int, TreeMap[Int, TreeMap[Int, VSet]]]()){
    case (cy, ((y,m,d),v)) => {
      val cm = cy.get(y).getOrElse(TreeMap[Int, TreeMap[Int, VSet]]())
      val cd = cm.get(m).getOrElse(TreeMap[Int, VSet]())
      cy + (y -> (cm + (m -> (cd + (d -> v)))))
    }
  }
}
