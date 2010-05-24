package ca.williams_family
package akka

import net.liftweb.common._
import Box._
import model._

import se.scalablesolutions.akka.actor._

trait PhotoStorage extends Transactor {
  type V = String
  type K = String

  def receive = {
    case CountPhotos =>
      self.reply(size)

    case GetPhotoIds =>
      self.reply(keys)

    case SetPhoto(photo) =>
      setPhoto(photo, Photo.serialize(photo))
      self.reply(true)

    case GetPhoto(id) =>
      self.reply(getPhoto(id).map(Photo.deserialize))

    case ForEachPhoto(f) =>
      foreach(v => f(Photo.deserialize(v)))
  }

  def get(k: K): Option[V]

  def put(k: K, v: V): Unit

  def size: Int

  def keys: Iterable[K]

  def foreach(f: (V) => Unit): Unit

  def setPhoto(photo: Photo, v: V): Unit = put(photo.id, v)

  def getPhoto(k: K): Option[V] = get(k)

}
