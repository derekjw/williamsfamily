package ca.williams_family
package akka

import model._

import se.scalablesolutions.akka.actor._


sealed trait Message

case class SetUser(user: User) extends Message

case class GetUser(id: Long) extends Message

case object CountPhotos extends Message

case object GetPhotoIds extends Message

case class SetPhoto(photo: Photo) extends Message

case class SetPhotoTxn(photo: Photo, actors: List[ActorRef])

case class GetPhoto(id: String) extends Message

case class GetPhotos(ids: List[String]) extends Message

case class GetPhotoTimeline(year: Option[Int] = None, month: Option[Int] = None, day: Option[Int] = None) extends Message

object GetPhotoTimeline {
  def apply(y: Int): GetPhotoTimeline = apply(Some(y))
  def apply(y: Int, m: Int): GetPhotoTimeline = apply(Some(y), Some(m))
  def apply(y: Int, m: Int, d: Int): GetPhotoTimeline = apply(Some(y), Some(m), Some(d))
  def apply(in: List[Int]): GetPhotoTimeline = in match {
    case y :: m :: d :: _ => apply(y,m,d)
    case y :: m :: Nil => apply(y,m)
    case y :: Nil => apply(y)
    case Nil => apply()
  }
}

case class ReIndex(photo: Photo) extends Message

case class ForEachPhoto(fun: (Photo) => Unit) extends Message

case class WithPhoto(fun: (Photo) => Unit) extends Message
