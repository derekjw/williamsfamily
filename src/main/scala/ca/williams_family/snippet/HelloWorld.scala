package ca.williams_family
package snippet

import scala.xml._
import net.liftweb.util.Helpers._
import net.liftweb.common._
import Box._
import net.liftweb.http.{S,SHtml}
import net.liftweb.http.js.{JE,JsCmds,JsExp}
import JE._
import JsCmds._
import net.liftweb.http.js.jquery._
import JqJE._
import JqJsCmds._

import akka._
import model._
import lib.Js._

import se.scalablesolutions.akka.actor.ActorRegistry

class HelloWorld {
  lazy val photos = ActorRegistry.actorsFor[PhotoService].head

  def updateFragment =
    JqAjax(url = "/ajaxloc", data = Stringify(JsObj("fragment" -> JsVar("window", "location", "hash"))))

  def tester(in: NodeSeq): NodeSeq =
    Script(OnLoad(
      Jq(JsVar("window")) ~> JqBind("hashchange", AnonFunc(updateFragment)) &
      Jq(JsVar("window")) ~> JqTrigger("hashchange")
    ))

  def howdy(in: NodeSeq): NodeSeq =
    for {
      query <- List("testid", "testid", "failedlookup", "testid")
      photo <- photos.getPhoto(query)
    } yield {
      bind("b", in, "time" -> photo.createDate.mkString)
    }

  implicit def unboxNodeSeq(in: Box[NodeSeq]): NodeSeq = in match {
    case x @ Failure(_,_,_) => Text("Failure: "+x.messageChain)
    case Full(x) => x
    case _ => Nil
  }

}

