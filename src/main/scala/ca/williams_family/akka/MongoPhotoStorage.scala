package ca.williams_family
package akka

import net.liftweb.common._
import Box._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm.Transaction.Local._
import se.scalablesolutions.akka.persistence.mongo.MongoStorage
import se.scalablesolutions.akka.config.ScalaConfig._

trait MongoPhotoStorageFactory {
  self: PhotoService =>
  val storage: PhotoStorage = spawnLink[MongoPhotoStorage]
}

class MongoPhotoStorage extends PhotoStorage {
  lifeCycle = Some(LifeCycle(Permanent))

  private val photos = atomic { MongoStorage.getMap("photos") }

  def get(k: K): Option[V] = atomic { photos.get(k).asA[String] }

  def put(k: K, v: V): Unit = atomic { photos.put(k, v) }

  def size: Int = atomic { photos.size }

  def keys: Iterable[K] = atomic { photos.keysIterator.map(_.toString).toList }

  def foreach(f: (V) => Unit) = atomic { photos.valuesIterator.map(_.toString).foreach(f) }

}

