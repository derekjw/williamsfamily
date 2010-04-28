package ca.williams_family
package akka

import net.liftweb.common._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm.Transaction.Local._
import se.scalablesolutions.akka.persistence.redis.RedisStorage
import se.scalablesolutions.akka.config.ScalaConfig._

trait RedisPhotoStorageFactory {
  self: PhotoService =>
  val storage: PhotoStorage = spawnLink[RedisPhotoStorage]
}

class RedisPhotoStorage extends PhotoStorage with Logger with RedisHelpers {
  lifeCycle = Some(LifeCycle(Permanent))

  private lazy val photos = atomic { RedisStorage.getMap("photos") }

  def get(k: K): Option[V] = photos.get(k).map(asString)

  def put(k: K, v: V): Unit = photos.put(k, v)

  def size: Int = photos.size
}

