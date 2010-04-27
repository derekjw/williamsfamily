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

  private val index = atomic { TransactionalState.newMap[Int, idxSet] }

  def getSet(key: Int): idxSet =
    index.get(key).getOrElse(set())

  def putSet(key: Int, newSet: idxSet): Unit =
    index.put(key, newSet)
}
