package ca.williams_family
package akka

import net.liftweb.common._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm.Transaction.Local._
import se.scalablesolutions.akka.persistence.mongo.MongoStorage
import se.scalablesolutions.akka.config.ScalaConfig._
/*
trait MongoPhotoStorageFactory {
  self: PhotoService =>
  val storage: PhotoStorage = spawnLink[MongoPhotoStorage]
}

class MonogoPhotoStorage extends PhotoStorage {
  lifeCycle = Some(LifeCycle(Permanent))

  private val photos = atomic { MongoStorage.getMap("photos") }

  type V = AnyRef
  type K = String

  def receive = {
    case CountPhotos =>
      reply(size)

    case GetPhotoIds =>
      reply(keys)

    case msg @ SetPhoto(photo, _) =>
      setPhoto(photo)
      reply(true)

    case GetPhoto(id) =>
      reply(getPhoto(id))

    case ForEachPhoto(f) =>
      Actor.spawn {
        foreach(f)
        reply(true)
      }
  }

  def setPhoto(photo: K): Unit = put(photo.id, photo)

  def getPhoto(k: K): Option[V] = get(k)

  def get(k: K): Option[V] = atomic { photos.get(k) }

  def put(k: K, v: V): Unit = atomic { photos.put(k, v) }

  def size: Int = atomic { photos.size }

  def keys: Iterable[K] = atomic { photos.keysIterator.toList }

  def foreach(f: (V) => Unit) = atomic { photos.valuesIterator.foreach(f) }

}

*/
