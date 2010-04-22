package ca.williams_family
package lib

import net.liftweb.http.js._
import JE._
import JsCmds._
import jquery._


object Js {
  case class JqBind(event: String, exp: JsExp) extends JsExp with JsMember {
    def toJsCmd = "bind('"+event+"', "+exp.toJsCmd+")"
  }

  case class JqTrigger(event: String) extends JsExp with JsMember {
    def toJsCmd = "trigger('"+event+"')"
  }

  case class JqAjax(url: JsExp, data: JsExp, requestType: JsExp = Str("POST"), dataType: JsExp = Str("script"), contentType: JsExp = Str("text/json"), timeout: JsExp = Num(5000), async: JsExp = JsTrue, cache: JsExp = JsFalse) extends JsExp with JsMember {
    def toJsCmd = "jQuery.ajax({ url : "+url.toJsCmd+", data : "+data.toJsCmd+", type : "+requestType.toJsCmd+", dataType : "+dataType.toJsCmd+", contentType: "+contentType.toJsCmd+", timeout : "+timeout.toJsCmd+", cache : "+cache.toJsCmd+", async : "+async.toJsCmd+"})"
  }
}
