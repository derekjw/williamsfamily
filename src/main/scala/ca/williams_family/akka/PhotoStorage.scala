package ca.williams_family
package akka

import model._

import se.scalablesolutions.akka.actor._

trait PhotoStorage extends Actor {
  type V = String
  type K = String

  val photoSerializer = new PhotoSerializer(this)
  startLink(photoSerializer)

  def receive = {
    case CountPhotos => reply(size)
    case msg @ SetPhoto(photo, None) => photoSerializer forward msg
    case SetPhoto(photo, Some(json)) => {
      setPhoto(photo, json)
      reply(true)
    }
    case GetPhoto(id) => reply(getPhoto(id).map(Photo.deserialize))
  }

  def get(k: K): Option[V]

  def put(k: K, v: V): Unit

  def size: Int

  def setPhoto(photo: Photo, v: V): Unit = put(photo.id, v)

  def getPhoto(k: K): Option[V] = get(k)

  override def shutdown = {
    unlink(photoSerializer)
    photoSerializer.stop
  }

}
