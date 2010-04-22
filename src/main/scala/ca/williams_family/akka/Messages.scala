package ca.williams_family
package akka

sealed trait Message

case object CountPhotos extends Message
case class SetPhoto(id: String, json: String) extends Message
case class GetPhoto(id: String) extends Message
