package ca.williams_family
package model

import xml.{Node}

import akka._

import net.liftweb.common._
import Box._
import net.liftweb.util._
import net.liftweb.http._
import net.liftweb.json._
import JsonAST._
import JsonDSL._
import JsonParser._
import Serialization.{read, write}

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.dispatch._

import Helpers._

import net.liftweb.ext_api.facebook.{FacebookClient, FacebookSession, GetInfo, Name, SquarePic, Username, Email}

case class User(id: Long, userName: String, name: String, photoUri: String) {
  def save: Boolean = true
  def validate: List[FieldError] = Nil
}

object User {
  private val noService = Failure("User service not set")
  private var _service: Box[ActorRef] = noService

  def service = _service

  def service_=(us: ActorRef): Unit = _service = Full(us.start)

  def stopService: Unit = {
    _service.foreach(_.stop)
    _service = noService
  }

  def get(id: Long) =
    for {
      s <- service
      res <- ((s !! GetUser(id)) ?~ "Timed out" ~> 500).asA[Option[User]]
      user <- res ?~ "User Not Found" ~> 404
    } yield user

  def set(user: User): Box[Future[Boolean]] = 
    for {
      s <- service
    } yield {s !!! SetUser(user)}


  def serialize(in: User) = {
    implicit val formats = DefaultFormats
    write(in)
  }

  def deserialize(in: String) = {
    implicit val formats = DefaultFormats
    read[User](in)
  }

  def fromXml(in: Node): Box[User] =
    for {
      id <- asLong((in \\ "uid").text)
      userName <- Full((in \\ "username").text)
      name <- Full((in \\ "name").text)
      photoUri <- Full((in \\ "pic_square").text)
    } yield User(id,userName,name,photoUri)

  def loginFromFacebook(session: FacebookSession): Box[User] = {
    fbSession(Full(session))
    for {
      user <- fromXml(FacebookClient.fromSession(session).getInfo(asLong(session.uid), Name, Username, SquarePic))
    } {
      set(user)
      logUserIn(user)
    }
    currentUser
  }

  def logUserIn(user: User) {
    println("Logging in user "+user)
    curUser.remove()
    curUser(Full(user))
    curUserId(Full(user.id))
  }

  def logUserOut() {
    currentUser.foreach(u => println("Logging out user "+u))
    curUserId.remove()
    curUser.remove()
    S.request.foreach(_.request.session.terminate)
  }

  def notLoggedIn_? = !loggedIn_?

  def loggedIn_? = currentUserId.isDefined

  private object curUserId extends SessionVar[Box[Long]](Empty)

  def currentUserId: Box[Long] = curUserId.is

  private object curUser extends RequestVar[Box[User]](
    for {
      id <- curUserId
      user <- get(id)
    } yield user)

  def currentUser: Box[User] = curUser.is

  private object fbSession extends SessionVar[Box[FacebookSession]](Empty)

  def facebookSession: Box[FacebookSession] = fbSession

}
