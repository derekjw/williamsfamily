package ca.williams_family
package akka

import net.liftweb.common._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm._
import se.scalablesolutions.akka.stm.Transaction.Local._
import se.scalablesolutions.akka.config.ScalaConfig._

trait InMemoryPhotoStorageFactory {
  self: Actor =>
  val storage: PhotoStorage = spawnLink[InMemoryPhotoStorage]
}

class InMemoryPhotoStorage extends StringPhotoStorage {
  lifeCycle = Some(LifeCycle(Permanent))

  private val photos = atomic { TransactionalState.newMap[K, V] }

  def get(k: K): Option[V] = atomic { photos.get(k) }

  def put(k: K, v: V): Unit = atomic { photos.put(k, v) }

  def size: Int = atomic { photos.size }

  def keys: Iterable[K] = atomic { photos.keysIterator.toList }

  def foreach(f: (V) => Unit) = atomic { photos.valuesIterator.foreach(f) }
}

