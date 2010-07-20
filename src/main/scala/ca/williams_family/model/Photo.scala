package ca.williams_family
package model

import akka._

import net.fyrie.ratio._

import net.liftweb.common._
import Box._
import net.liftweb.util.Helpers._
import net.liftweb.json._
import JsonAST._
import JsonDSL._
import JsonParser._
import Serialization.{read, write}

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.dispatch._

case class Photo(id: String, createDate: List[Int], exposure: Ratio, aperature: Ratio, iso: Int, focalLength: Ratio, width: Int, height: Int, images: Map[String, Image]) {
  def uri = "/photos/"+id
  def toJson = {
    ("id", id) ~
    ("uri", uri) ~
    ("createdate", Photo.mkDate(createDate)) ~
    ("images", JObject(images.map{case (k,v) => JField(k, v.toJson)}.toList))
  }
}

object Photo {
  private val noService = Failure("Photo service not set")
  private var _service: Box[ActorRef] = noService

  def service = _service

  def service_=(ps: ActorRef): Unit = {
    _service = Box !! ps.start orElse noService
//    reIndex
  }

  def stopService: Unit = {
    _service.foreach(_.stop)
    _service = noService
  }


  def reIndex: Unit =
    logTime("ReIndexing photos"){
      for {
        s <- service
        id <- ((s !! GetPhotoIds) ?~ "Timed out").asA[List[String]].getOrElse(Nil)
        photo <- get(id)
      } { s ! ReIndex(photo) }
    }

  def count =
    for {
      s <- service
      res <- ((s !! CountPhotos) ?~ "Timed out").asA[java.lang.Integer].map(_.intValue)
    } yield res

  def set(photo: Photo): Unit =
    for {
      s <- service
    } s ! SetPhoto(photo)

  def get(id: String) =
    for {
      s <- service
      res <- ((s !! GetPhoto(id)) ?~ "Timed out" ~> 500).asA[Option[Photo]] ?~ "Invalid Response"
      photo <-res ?~ "Photo Not Found" ~> 404
    } yield photo

  def timeline(key: List[Int]): Box[PhotoTimeline] =
    for {
      s <- service
      res <- ((s !! GetPhotoTimeline(key)) ?~ "Timed out").asA[PhotoTimeline] ?~ "Invalid Response"
    } yield res

  def timeline(year: Int = 0, month: Int = 0, day: Int = 0): Box[PhotoTimeline] = timeline(List(year,month,day).filterNot(_ == 0))

  def serialize(in: Photo) = {
    implicit val formats = DefaultFormats + RatioSerializer
    write(in)
  }

  def deserialize(in: String) = {
    implicit val formats = DefaultFormats + RatioSerializer
    read[Photo](in)
  }

  def mkId(date: List[Int], hash: String) = date match {
    case year :: month :: day :: hour :: minute :: second :: msecond :: rest =>
      "%04d%02d%02d-%02d%02d%02d%02d-%s".format(year, month, day, hour, minute, second, msecond, hash)
  }

  def mkDate(date: List[Int]) =
    "%04d-%02d-%02dT%02d:%02d:%02d.%02d" format (date:_*)

}

case class Image(fileName: String, fileSize: Int, hash: String, width: Int, height: Int) {
  def uri = "http://photos.williams-family.ca/photos/"+fileName

  def toJson: JValue = {
    ("filename" -> fileName) ~
    ("filesize" -> fileSize) ~
    ("uri" -> uri) ~
    ("hash" -> hash) ~
    ("width" -> width) ~
    ("height" -> height)
  }
}

object RatioSerializer extends Serializer[Ratio] {
  private val RatioClass = classOf[Ratio]
  private val RatioRegex = """^\s*(\d+)\s*/\s*(\d+)\s*$""".r
  private val IntRegex = """^\s*(\d+)\s*$""".r

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Ratio] = {
    case (TypeInfo(RatioClass, _), json) => json match {
      case JString(RatioRegex(n,d)) => Ratio(BigInt(n), BigInt(d))
      case JString(IntRegex(n)) => Ratio(BigInt(n))
      case x => throw new MappingException("Can't convert "+x+" to Ratio")
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: Ratio => JString(x.toString)
  }
}

