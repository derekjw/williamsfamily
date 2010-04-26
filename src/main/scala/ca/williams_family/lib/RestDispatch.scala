package ca.williams_family
package lib

import net.liftweb._
import http._
import common._
import Box._
import json._
import json.Serialization.{read, write}
import json.Extraction.{decompose}
import se.scalablesolutions.akka.actor.ActorRegistry

object RestDispatch {
  def init() {
    LiftRules.statelessDispatchTable.append {
      case r @ Req("api" :: "photos" :: photoId :: Nil, _, GetRequest) => () => getPhoto(r, photoId)
      case r @ Req("api" :: "photos" :: Nil, _, PostRequest) => () => postPhoto(r)
    }
  }

  implicit val formats = Serialization.formats(NoTypeHints)

  def postPhoto(req: Req): Box[LiftResponse] = 
    for {
      json <- req.json ~> "No json received"
      photo = read[model.Photo](json.toString)
      photoService <- ActorRegistry.actorsFor[akka.PhotoService].headOption ?~ "Photo service not running" ~> 500
    } yield {
      photoService.setPhoto(photo)
      AcceptedResponse()
    }

  def getPhoto(req: Req, photoId: String): Box[LiftResponse] =
    for {
      photoService <- ActorRegistry.actorsFor[akka.PhotoService].headOption ?~ "Photo service not running" ~> 500
      photo <- photoService.getPhoto(photoId)
    } yield {
      JsonResponse(decompose(photo))
    }
}
