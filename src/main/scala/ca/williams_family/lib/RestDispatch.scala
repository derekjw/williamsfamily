package ca.williams_family
package lib

import model._

import net.liftweb._
import http._
import rest._
import common._
import Box._
import json._
import JsonAST._
import util.Helpers._

object RestServices extends RestHelper {

  serve {
    case JsonGet("photos" :: photoId :: Nil, _) => getPhoto(photoId)
    //case Put("api" :: "photos" :: Nil, json) => postPhoto(json)

    case req @ JsonGet("timeline" :: TimelineDate(date), _) => getTimeline(date, req.param("after").getOrElse(""))
  }

  def getTimeline(date: List[Int], after: String): Box[JValue] =
    for {
      res <- Photo.timeline(date)
    } yield { JArray(res.dropWhile(_ <= after).take(1000).map(JString).toList) }

  object TimelineDate {
    def unapply(in: List[String]): Option[List[Int]] = tryo(in.map(_.toInt))
  }


/*  def postPhoto(req: Req): Box[LiftResponse] = 
    for {
      json <- req.json ~> "No json received"
      photo = read[model.Photo](json.toString)
      photoService <- ActorRegistry.actorsFor[akka.PhotoService].headOption ?~ "Photo service not running" ~> 500
    } yield {
      photoService.setPhoto(photo)
      AcceptedResponse()
    }*/

  def getPhoto(photoId: String): Box[JValue] =
    for {
      photo <- Photo.get(photoId) ?~ "Photo not found" ~> 404
    } yield photo.toJson

}
