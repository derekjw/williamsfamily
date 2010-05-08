package ca.williams_family
package model

import xml.{Node}

import akka._

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.http._
import net.liftweb.json._
import JsonAST._
import JsonDSL._
import JsonParser._
import Serialization.{read, write}

import Helpers._

import net.liftweb.ext_api.facebook.{FacebookClient, FacebookSession, GetInfo, Name, SquarePic, Username, Email}

case class User(id: Long, userName: String, name: String, photoUri: String) {
  def save: Boolean = true
  def validate: List[FieldError] = Nil
}

object User {
  private var userService: Box[UserService] = Failure("User service not set")

  def service = userService

  def service_=(us: UserService): Unit = userService = Full(us)

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
      us <- service
    } {
      us.setUser(user)
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
      us <- service
      id <- curUserId
      user <- us.getUser(id)
    } yield user)

  def currentUser: Box[User] = curUser.is

  private object fbSession extends SessionVar[Box[FacebookSession]](Empty)

  def facebookSession: Box[FacebookSession] = fbSession

}
