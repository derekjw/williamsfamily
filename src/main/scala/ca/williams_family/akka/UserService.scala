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
  faultHandler = Some(OneForOneStrategy(5, 5000))
  trapExit = List(classOf[Exception])

  val storage: ActorID

  def getUser(id: Long) =
    for {
      res <- ((self !! GetUser(id)) ?~ "Timed out" ~> 500).asA[Option[User]]
      user <- res ?~ "User Not Found" ~> 404
    } yield user

  def setUser(user: User): Future[Boolean] = self !!! SetUser(user)

  def receive = {
    case msg: SetUser => storage forward msg
    case msg: GetUser => storage forward msg
  }

  override def shutdown = {
    unlink(storage)
    storage.stop
  }
}
