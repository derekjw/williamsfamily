package ca.williams_family
package akka

import model._

sealed trait Message

case object CountPhotos extends Message
case class SetPhoto(photo: Photo, json: Option[String]) extends Message
case class GetPhoto(id: String) extends Message
case class GetPhotos(ids: List[String]) extends Message
