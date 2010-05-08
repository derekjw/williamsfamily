package ca.williams_family
package model

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.http._

import Helpers._

case class User(fbid: Long, email: String) {
  def id = email
  def save: Boolean = true
  def validate: List[FieldError] = Nil
}

object User {
  def find(email: String): Box[User] = Empty
  def findByFbId(fbid: Long): Box[User] = Full(User(fbid, "test@example.com"))
  def findByFbId(fbid: String): Box[User] = 
    for {
      fbidValid <- asLong(fbid)
      res <- findByFbId(fbidValid)
    } yield res

  def logUserIn(user: User) {
    println("Logging in user "+user)
    curUser.remove()
    curUserId(Full(user.id))
    onLogIn.foreach(_(who))
  }

  def logUserOut() {
    println("Logging out user "+user)
    onLogOut.foreach(_(curUser))
    curUserId.remove()
    curUser.remove()
    S.request.foreach(_.request.session.terminate)
  }

  def notLoggedIn_? = !loggedIn_?

  def loggedIn_? = {
    if(!currentUserId.isDefined)
      for(f <- autologinFunc) f()
    currentUserId.isDefined
  }

  var autologinFunc: Box[()=>Unit] = Empty

  private object curUserId extends SessionVar[Box[String]](Empty)

  def currentUserId: Box[String] = curUserId.is

  private object curUser extends RequestVar[Box[User]](curUserId.flatMap(id => find(id)))

  def currentUser: Box[User] = curUser.is

}
