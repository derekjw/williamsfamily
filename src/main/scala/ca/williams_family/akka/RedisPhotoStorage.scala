package ca.williams_family
package akka

import net.liftweb.common._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.config.ScalaConfig._

import com.redis._

trait RedisPhotoStorageFactory {
  this: PhotoService =>
  val storage: ActorRef = this.self.spawnLink[RedisPhotoStorage]
}

class RedisPhotoStorage extends PhotoStorage with RedisHelpers {
  self.lifeCycle = Some(LifeCycle(Permanent))

  val r = new RedisClient("localhost", 6379)

  val ns = "photo"
  val keySet = "photos"

  def key(k: K) = ns+":"+k

  def get(k: K): Option[V] = r.get(key(k))

  def put(k: K, v: V): Unit = {
    r.multi
    r.set(key(k), v)
    r.sadd(keySet, k)
    r.exec
  }

  def size: Int = r.scard(keySet).getOrElse(0)

  def keys: Iterator[K] = r.smembers(keySet).map(_.iterator.flatMap(_.iterator)).getOrElse(Iterator.empty)

}

