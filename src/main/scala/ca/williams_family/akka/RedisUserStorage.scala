package ca.williams_family
package akka

import net.liftweb.common._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.config.ScalaConfig._

trait RedisUserStorageFactory {
  this: UserService =>
  val storage: ActorRef = null //this.self.spawnLink[RedisUserStorage]
}
/*
class RedisUserStorage extends UserStorage with RedisHelpers {
  self.lifeCycle = Some(LifeCycle(Permanent))

  private var users = RedisStorage.getMap("users")

  def get(k: K): Option[V] = atomic { users.get(k).map(asString) }

  def put(k: K, v: V): Unit = atomic { users.put(k, v) }

  override def postRestart(reason: Throwable) =
    users = RedisStorage.getMap("photos")

}
*/
