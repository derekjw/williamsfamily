package ca.williams_family
package akka

import net.liftweb.common._
import Box._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonParser.parse

import collection.SortedSet

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm.Transaction.Local._
import se.scalablesolutions.akka.persistence.redis.RedisStorage
import se.scalablesolutions.akka.config.ScalaConfig._
/*
class RedisPhotoDateIndex extends PhotoDateIndex with RedisHelpers {
  lifeCycle = Some(LifeCycle(Permanent))

  private val idx = atomic { RedisStorage.getMap("photoDateIndex") }

  def get(k: K): V =
    idx.get(k).map(b => parse(b).values).asA[List[String]].map(l => nV(l:_*)).getOrElse(nV())

  def put(k: K, v: V): Unit =
    idx.put(k, compact(render(v.toList)))

}
*/
