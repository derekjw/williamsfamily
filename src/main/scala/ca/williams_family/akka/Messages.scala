package ca.williams_family
package akka

import model._

sealed trait Message

case object CountPhotos extends Message
case object GetPhotoIds extends Message
case class SetPhoto(photo: Photo) extends Message
case class GetPhoto(id: String) extends Message
case class GetPhotos(ids: List[String]) extends Message
case class GetPhotosByDate(date: List[Int] = Nil) extends Message
case class RegisterPhotoIndex(index: PhotoIndex) extends Message
case class ForEachPhoto(fun: (Photo) => Unit) extends Message
case class WithPhoto(fun: (Photo) => Unit) extends Message
