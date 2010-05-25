package ca.williams_family
package akka

import net.liftweb.common._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.config.ScalaConfig._

trait InMemoryPhotoStorageFactory {
  this: Actor =>
  val storage: ActorRef = this.self.spawnLink[InMemoryPhotoStorage]
}

class InMemoryPhotoStorage extends PhotoStorage {
  self.lifeCycle = Some(LifeCycle(Permanent))

  private var photos = Map[K, V]()

  def get(k: K): Option[V] = photos.get(k)

  def put(k: K, v: V): Unit = photos += (k -> v)

  def size: Int = photos.size

  def keys: Iterator[K] = photos.keysIterator

  def foreach(f: (V) => Unit) = photos.valuesIterator.foreach(f)

}

