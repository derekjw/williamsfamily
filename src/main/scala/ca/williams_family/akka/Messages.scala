package ca.williams_family
package akka

import model._

sealed trait Message

case object CountPhotos extends Message
case class SetPhoto(photo: Photo, json: Option[String] = None) extends Message
case class GetPhoto(id: String) extends Message
case class GetPhotos(ids: List[String]) extends Message
case class GetPhotosByDate(date: Int) extends Message
case class RegisterPhotoIndex(index: PhotoIndex) extends Message
case class SerializedPhoto(json: String) extends Message
