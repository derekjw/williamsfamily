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
import util.Helpers._

import collection.immutable.{SortedMap,SortedSet}
import xml._

object AjaxDispatch {
  type DPF = LiftRules.DispatchPF

  def init() {
    LiftRules.dispatch.prepend(table)
  }

  def table = getAjaxLoc

  def getAjaxLoc: DPF = {
    case r @ Req("ajaxloc" :: Nil, _, PostRequest) =>
      for {
        json <- r.json ?~ "No json received" ~> 400
        fragment <- json \ "fragment" \ classOf[JString] ?~ "Invalid json received" ~> 400
        ajaxLoc <- AjaxLoc.parse(fragment) ?~ "Invalid location requested" ~> 400
        found <- ajaxLocDispatch.lift(ajaxLoc) ?~ "Not found" ~> 404
        jsCmd <- found
      } yield jsCmd
  }

  def ajaxLocDispatch: PartialFunction[AjaxLoc, Box[JsCmd]] = {

    case a @ AjaxLoc("photos" :: Nil, p, _) =>
      Full(<div><a href={a.toUri}>Photos({p})</a></div>)

    case a @ AjaxLoc("photos" :: id :: Nil, p, _) =>
      Full(<div><a href={a.toUri}>Photo[{id}]({a.paramList})</a></div>)

  }

  implicit def iterableToBox[X](in: Iterable[X]): Box[X] = in.headOption

  implicit def responseFuncBuilder[T](value: T)(implicit cvt: T => LiftResponse): () => Box[LiftResponse] =
    () => handleFailure(Full(value))(cvt)

  implicit def responseFuncBuilder[T](value: Box[T])(implicit cvt: T => LiftResponse): () => Box[LiftResponse] =
    () => handleFailure(value)(cvt)

  implicit def handleFailure[T](value: Box[T])(implicit cvt: T => LiftResponse): Box[LiftResponse] = {
    value match {
      case ParamFailure(msg, _, _, code: Int) =>
        Full(InMemoryResponse(msg.getBytes("UTF-8"), ("Content-Type" -> "text/plain; charset=utf-8") :: Nil, Nil, code))
      case Failure(msg, _, _) => Full(NotFoundResponse(msg))
      case Empty => Empty
      case Full(x) => Full(cvt(x))
    }
  }
  
  implicit def jsToResponse(in: JsCmd): LiftResponse = JavaScriptResponse(in)

  implicit def xmlToJs(in: Elem): JsCmd = SetHtml("content", in)
}

case class AjaxLoc(path: List[String] = Nil, params: SortedMap[String,SortedSet[String]] = SortedMap(), baseUri: List[String] = Nil) {
  def toUri = appendParams("/"+baseUri.map(urlEncode).mkString("/")+"#"+path.map(urlEncode).mkString("/"), paramList)
  def paramList = params.toList.flatMap{case (k,vs) => vs.map(v => (k,v))}
}

object AjaxLoc {
  private val LocRegex = """^#([^?]*)\??(.*)$""".r

  def parse(in: String): Option[AjaxLoc] = in match {
    case LocRegex(path, "") => Some(AjaxLoc(parsePath(path)))
    case LocRegex(path, params) => Some(AjaxLoc(parsePath(path), parseParams(params)))
    case _ => None
  }

  private def parsePath(in: String) = Req.parsePath(in).partPath

  private def parseParams(in: String): SortedMap[String, SortedSet[String]] =
    in.split('&').toList.map(_.split('=').toList).foldLeft(SortedMap[String, SortedSet[String]]()){
      case (m, List(k,v)) => m + (k -> (m.get(k).getOrElse(SortedSet[String]()) + v))
      case (m, _) => m
    }
}
