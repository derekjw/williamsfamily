package ca.williams_family
package akka

import net.liftweb.common._
import Box._
import model._

import se.scalablesolutions.akka.actor._

trait UserStorage extends Actor {
  type V = String
  type K = String

  def receive = {
    case SetUser(user) =>
      setUser(user, User.serialize(user))
      reply(true)

    case GetUser(id) =>
      reply(getUser(id).map(User.deserialize))
  }

  def get(k: K): Option[V]

  def put(k: K, v: V): Unit

  def setUser(user: User, v: V): Unit = put(user.id.toString, v)

  def getUser(k: K): Option[V] = get(k)

  def getUser(k: Long): Option[V] = getUser(k.toString)

}
