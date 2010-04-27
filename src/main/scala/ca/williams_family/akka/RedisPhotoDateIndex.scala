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

class RedisPhotoDateIndex extends PhotoDateIndex with Logger {
  lifeCycle = Some(LifeCycle(Permanent))

  info("Redis photo date index is starting up.")

  private val index = atomic { RedisStorage.getMap("photoDateIndex") }

  def setPhoto(photo: Photo): Unit = {
    val indexId = photo.id.take(6)
    index.put(indexId, compact(render((index.get(indexId).map(b => parse(b).values).asA[List[String]].map(l => SortedSet(l:_*)).getOrElse(SortedSet[String]()) + photo.id).toList)))
  }

  private implicit def stringToByteArray(in: String): Array[Byte] = in.getBytes("UTF-8")
  private implicit def asString(in: Array[Byte]): String = new String(in, "UTF-8")
}
