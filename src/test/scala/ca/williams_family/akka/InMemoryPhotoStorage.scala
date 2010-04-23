package ca.williams_family
package akka

import net.liftweb.common._

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

  def receive = {
    case CountPhotos => reply(photos.size)
    case SetPhoto(id,json) => {
      info("Setting: "+json)
      photos.put(id,json)
    }
    case GetPhoto(id) => {
      info("Getting: "+photos.get(id))
      reply(photos.get(id))
    }
  }
}

