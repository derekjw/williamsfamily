package ca.williams_family
package akka

import model._

import se.scalablesolutions.akka.actor._

trait PhotoStorage extends Transactor {
  type V = String
  type K = String

  def receive = {
    case CountPhotos => reply(size)
    case SetPhoto(photo, json) => setPhoto(photo, json)
    case GetPhoto(id) => reply(getPhoto(id))
  }

  def get(k: K): Option[V]

  def put(k: K, v: V): Unit

  def size: Int

  def setPhoto(photo: Photo, v: V): Unit = put(photo.id,v)

  def getPhoto(k: K): Option[V] = get(k)
}
