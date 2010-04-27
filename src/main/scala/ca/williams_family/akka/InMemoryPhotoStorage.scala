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

class InMemoryPhotoStorage extends PhotoStorage with Logger {
  lifeCycle = Some(LifeCycle(Permanent))

  private val photos = atomic { TransactionalState.newMap[String, String] }

  def countPhotos = photos.size

  def setPhoto(photo: Photo): Unit = setPhoto(photo, Photo.serialize(photo))

  def setPhoto(photo: Photo, json: String): Unit = photos.put(photo.id,json)

  def getPhoto(id: String): Option[String] = photos.get(id)

}

