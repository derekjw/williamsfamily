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
import xml.NodeSeq

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
      JObject(List(JField("fragment", JString(LocationLink(loc))))) <- json
    } yield createResponse(loc)
  }

  def createResponse(loc: LocationLink): LiftResponse = loc.path match {
    case "photos" :: Nil => <div><a href={loc.toString}>Photos({loc.paramList})</a></div>
    case "photos" :: id :: Nil => <div><a href={loc.toString}>Photo[{id}]({loc.paramList})</a></div>
    case path => <div>Other: {path.toString}({loc.paramList})</div>
  }

  private implicit def nodeSeqToLiftResponse(in: NodeSeq): LiftResponse = JavaScriptResponse(SetHtml("content", in))

}

class LocationLink(val path: List[String], val params: SortedMap[String,SortedSet[String]] = SortedMap(), val baseUri: String = "/") {
  override def toString = baseUri+"#"+path.mkString("/") + (if (params.size > 0) ("?" + paramsToUrlParams(paramList)) else "")
  def copy(newPath: List[String] = path, newParams: SortedMap[String,SortedSet[String]] = params, newBaseUri: String = baseUri) = new LocationLink(newPath, newParams, newBaseUri)
  def paramList = params.toList.flatMap{case (k,vs) => vs.map(v => (k,v))}
}

object LocationLink {
  private val LocationRegex = """^#([^?]*)\??(.*)$""".r

  def apply(path: List[String], params: SortedMap[String,SortedSet[String]]) = new LocationLink(path, params)
  def apply(path: List[String]) = new LocationLink(path)
  def apply(in: String): Option[LocationLink] = unapply(in)
  def unapply(in: String): Option[LocationLink] = in match {
    case LocationRegex(path, "") => Some(LocationLink(parsePath(path)))
    case LocationRegex(path, params) => Some(LocationLink(parsePath(path), parseParams(params)))
    case _ => None
  }
  def unapply(in: LocationLink): Option[(List[String], SortedMap[String, SortedSet[String]])] = Some((in.path, in.params))

  private def parsePath(in: String) = Req.parsePath(in).partPath

  private def parseParams(in: String): SortedMap[String, SortedSet[String]] =
    in.split('&').toList.map(_.split('=').toList).foldLeft(SortedMap[String, SortedSet[String]]()){
      case (m, List(k,v)) => m + (k -> (m.get(k).getOrElse(SortedSet[String]()) + v))
      case (m, _) => m
    }
}
