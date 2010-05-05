package ca.williams_family
package lib

import model._

import net.liftweb._
import http._
import rest._
import common._
import Box._
import json._
import json.Extraction.{decompose}

object RestServices extends RestHelper {

  serveJx {
    case Get("api" :: "photos" :: photoId :: Nil, _) => getPhoto(photoId)
    //case Put("api" :: "photos" :: Nil, json) => postPhoto(json)
    //case Get("api" :: "timeline" :: date, _) => getTimeline(date)
  }

  //def getTimeline(date: List[Int]): Box[Convertable]

/*  def postPhoto(req: Req): Box[LiftResponse] = 
    for {
      json <- req.json ~> "No json received"
      photo = read[model.Photo](json.toString)
      photoService <- ActorRegistry.actorsFor[akka.PhotoService].headOption ?~ "Photo service not running" ~> 500
    } yield {
      photoService.setPhoto(photo)
      AcceptedResponse()
    }*/

  def getPhoto(photoId: String): Box[Convertable] =
    for {
      ps <- Photo.service
      photo <- ps.getPhoto(photoId) ?~ "Photo not found" ~> 404
    } yield photo

  implicit def cvt: JxCvtPF[Convertable] = {
    case (JsonSelect, c, _) => c.toJson
    case (XmlSelect, c, _) => c.toXml
  }

}
