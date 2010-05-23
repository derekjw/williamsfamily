package ca.williams_family
package akka

import net.liftweb.common._
import Box._
import net.liftweb.util.Helpers._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.dispatch._
import Futures._
import se.scalablesolutions.akka.config.ScalaConfig._
import se.scalablesolutions.akka.config._

class RedisUserService extends UserService with RedisUserStorageFactory

abstract class UserService extends Actor with Logger {
  self.faultHandler = Some(OneForOneStrategy(5, 5000))
  self.trapExit = List(classOf[Exception])

  val storage: ActorRef

  def receive = {
    case msg: SetUser => storage forward msg
    case msg: GetUser => storage forward msg
  }

  override def shutdown = {
    self.unlink(storage)
    storage.stop
  }
}
