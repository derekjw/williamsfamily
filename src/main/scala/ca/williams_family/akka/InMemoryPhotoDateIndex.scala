package ca.williams_family
package akka

import net.liftweb.common._

import model._

import collection.SortedSet

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm._
import se.scalablesolutions.akka.stm.Transaction.Local._
import se.scalablesolutions.akka.config.ScalaConfig._

class InMemoryPhotoDateIndex extends PhotoDateIndex with Logger {
  lifeCycle = Some(LifeCycle(Permanent))

  private val index = atomic { TransactionalState.newMap[String, SortedSet[String]] }

  def setPhoto(photo: Photo): Unit = {
    val indexId = photo.id.take(6)
    index.put(indexId, index.get(indexId).getOrElse(SortedSet[String]()) + photo.id)
  }

}
