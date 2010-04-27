package ca.williams_family
package akka

import net.liftweb.common._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm._
import se.scalablesolutions.akka.stm.Transaction.Local._
import se.scalablesolutions.akka.config.ScalaConfig._

trait InMemoryPhotoDateIndexFactory {
  self: PhotoService =>
  val photoDateIndex: PhotoDateIndex = spawnLink[InMemoryPhotoDateIndex]
}

class InMemoryPhotoDateIndex extends PhotoDateIndex with Logger {
  lifeCycle = Some(LifeCycle(Permanent))

  private val photoDateIndex = atomic { TransactionalState.newMap[String, String] }

  def setPhoto(photo: Photo): Unit = {}

}
