package ca.williams_family
package snippet

import model._

import scala.xml._

import net.liftweb.common._
import net.liftweb.common.Box._
import net.liftweb.util._
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JE
import net.liftweb.http.js._
import net.liftweb.http.{S,SHtml}

import JsCmds._
import JE._

import Helpers._

import net.liftweb.ext_api.facebook.{FacebookRestApi,FacebookClient, FacebookConnect, FacebookSession}

class UserSnippet {
  def initfb:NodeSeq = {
    Script(Call("FB.init", Str(FacebookRestApi.apiKey), Str("/xd_receiver.htm")))
  }

  def fbloginLink:NodeSeq = SHtml.a(Text("Login"),loginCmd)

  def fblogoutLink:NodeSeq = SHtml.a(Text("Logout"),logoutCmd)

  def requireSession(f: FacebookSession => JsCmd):JsCmd = {
    val ajaxLogin = SHtml.ajaxInvoke(() => {
      FacebookConnect.session match {
        case Full(session) => f(session)
        case _ => Alert("Failed to create Facebook session")
      }
    })._2

    Call("FB.Connect.requireSession", AnonFunc(ajaxLogin))
  }

  def loginCmd:JsCmd = requireSession { session =>
    User.loginFromFacebook(session)
    RedirectTo("/")
  }

  def logoutCmd: JsCmd = Call("FB.Connect.logout", AnonFunc{
    SHtml.ajaxInvoke(() => {
      User.logUserOut()
      RedirectTo("/")
    })._2
  })

}
