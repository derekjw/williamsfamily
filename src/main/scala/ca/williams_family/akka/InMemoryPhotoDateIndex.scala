package ca.williams_family
package akka

import net.liftweb.common._

import model._

import collection.SortedSet

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm._
import se.scalablesolutions.akka.stm.Transaction.Local._
import se.scalablesolutions.akka.config.ScalaConfig._

class InMemoryPhotoDateIndex extends PhotoDateIndex {
  lifeCycle = Some(LifeCycle(Permanent))

  private val index = atomic { TransactionalState.newMap[K, V] }

  def get(k: K): V =
    atomic { index.get(k).getOrElse(nV()) }

  def put(k: K, v: V): Unit =
    atomic { index.put(k, v) }
}
