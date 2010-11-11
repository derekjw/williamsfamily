package ca.williams_family.snippet

import scala.xml.{NodeSeq, Text, Elem, Group, MetaData, Null, UnprefixedAttribute, PrefixedAttribute}
import ca.williams_family.model._
import net.liftweb._
import common._
import Box._
import util._
import Helpers._
import http.S
import sitemap.SiteMap

class Photos {
  def show =
    ".photo [src]" #> (for {
      loc <- SiteMap.findLoc("Photo") ?~ "Loc not found"
      photo <- loc.currentValue.asA[Photo]
      image <- photo.images.get("preview") ?~ "Preview image not found"
    } yield uri(image))

  implicit def boxToNodeSeq(in: Box[NodeSeq]): NodeSeq = in match {
    case Full(n) => n
    case Failure(m,_,_) => Text(m)
    case Empty => Text("")
  }
}
