package ca.williams_family
package akka

import net.liftweb.common._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm._
import Transaction.Global._
import se.scalablesolutions.akka.config.ScalaConfig._

trait InMemoryPhotoStorageFactory {
  self: Actor =>
  val storage: ActorID = spawnLink[InMemoryPhotoStorage]
}

class InMemoryPhotoStorage extends PhotoStorage {
  lifeCycle = Some(LifeCycle(Permanent))

  private val photos = TransactionalState.newMap[K, V]

  def get(k: K): Option[V] = atomic { photos.get(k) }

  def put(k: K, v: V): Unit = atomic { photos.put(k, v) }

  def size: Int = atomic { photos.size }

  def keys: Iterable[K] = atomic { photos.keysIterator.toList }

  def foreach(f: (V) => Unit) = atomic { photos.valuesIterator.foreach(f) }
}

