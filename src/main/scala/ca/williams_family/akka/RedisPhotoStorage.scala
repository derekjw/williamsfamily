package ca.williams_family
package akka

import net.liftweb.common._

import se.scalablesolutions.akka.stm.Transaction.Local._
import se.scalablesolutions.akka.persistence.redis.RedisStorage
import se.scalablesolutions.akka.config.ScalaConfig._

class RedisPhotoStorage extends PhotoStorage with Logger {
  lifeCycle = Some(LifeCycle(Permanent))

  val name = "photos"

  info("Redis photo storage is starting up.")

  private lazy val photos = atomic { RedisStorage.getMap(name) }

  def receive = {
    case CountPhotos => reply(photos.size)
    case SetPhoto(id,json) => {
      photos.put(id,json)
    }
    case GetPhoto(id) => {
      reply(photos.get(id).map(asString))
    }
  }

  private implicit def stringToByteArray(in: String): Array[Byte] = in.getBytes("UTF-8")
  private implicit def asString(in: Array[Byte]): String = new String(in, "UTF-8")
}

