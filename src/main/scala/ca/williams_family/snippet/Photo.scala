package ca.williams_family.snippet

import scala.xml.{NodeSeq, Text, Elem, Group, MetaData, Null, UnprefixedAttribute, PrefixedAttribute}
import ca.williams_family.model._
import net.liftweb._
import common._
import Box._
import util.Helpers._
import http.S

class Photo {
  def show(html: NodeSeq) : NodeSeq = {
    for {
      id <- S.param("id") ?~ "No Id supplied"
      ps <- Photo.service
      photo <- ps.getPhoto(id) ?~ "Photo not found"
      image <- photo.images.get("preview") ?~ "Preview image not found"
    } yield {
      bind("p", html, AttrBindParam("src", Text("http://williams-family.ca/photos/"+image.fileName), "src"))
    }
  }

  implicit def boxToNodeSeq(in: Box[NodeSeq]): NodeSeq = in match {
    case Full(n) => n
    case Failure(m,_,_) => Text(m)
    case Empty => Text("")
  }
}
