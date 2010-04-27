package ca.williams_family
package akka

import net.liftweb.common._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm.Transaction.Local._
import se.scalablesolutions.akka.persistence.redis.RedisStorage
import se.scalablesolutions.akka.config.ScalaConfig._

trait RedisPhotoDateIndexFactory {
  self: PhotoService =>
  val photoDateIndex: PhotoDateIndex = spawnLink[RedisPhotoDateIndex]
}

class RedisPhotoDateIndex extends PhotoDateIndex with Logger {
  lifeCycle = Some(LifeCycle(Permanent))

  info("Redis photo date index is starting up.")

  private val photoDateIndex = atomic { RedisStorage.getMap("photoDateIndex") }

  def setPhoto(photo: Photo): Unit = {}

  private implicit def stringToByteArray(in: String): Array[Byte] = in.getBytes("UTF-8")
  private implicit def asString(in: Array[Byte]): String = new String(in, "UTF-8")
}
