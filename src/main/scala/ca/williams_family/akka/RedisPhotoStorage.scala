package ca.williams_family
package akka

import net.liftweb.common._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm._
import Transaction.Global._
import se.scalablesolutions.akka.persistence.redis.RedisStorage
import se.scalablesolutions.akka.config.ScalaConfig._

trait RedisPhotoStorageFactory {
  self: PhotoService =>
  val storage: ActorID = spawnLink[RedisPhotoStorage]
}

class RedisPhotoStorage extends PhotoStorage with RedisHelpers {
  lifeCycle = Some(LifeCycle(Permanent))

  private var photos = RedisStorage.getMap("photos")

  def get(k: K): Option[V] = atomic { photos.get(k).map(asString) }

  def put(k: K, v: V): Unit = atomic { photos.put(k, v) }

  def size: Int = atomic { photos.size }

  def keys: Iterable[K] = atomic { photos.keysIterator.map(asString).toList }

  def foreach(f: (V) => Unit) = atomic { photos.valuesIterator.map(asString).foreach(f) }

  override def postRestart(reason: Throwable) =
    photos = RedisStorage.getMap("photos")

}

