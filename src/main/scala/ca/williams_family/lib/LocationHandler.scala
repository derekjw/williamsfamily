package ca.williams_family
package lib

import net.liftweb._
import http._
import common._
import Box._
import net.liftweb.json._
import JsonAST._
import js._
import JE._
import JsCmds._

object LocationHandler {
  def init() {
    LiftRules.dispatch.prepend {
      case r @ Req("location" :: Nil, _, PostRequest) => () => handleJson(r)
    }
  }

  implicit def iterableToBox[X](in: Iterable[X]): Box[X] = in.toList.headOption

  def handleJson(req: Req): Box[LiftResponse] = {
    for {
      json <- req.json ?~ "No json received"
      JObject(List(JField("anchor", JString(anchor)))) <- json
    } yield JavaScriptResponse(SetHtml("content", <div>Found: {anchor}</div><lift:HelloWorld.howdy><p><b:time/></p></lift:HelloWorld.howdy>))
  }
}
