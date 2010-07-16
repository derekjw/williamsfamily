package ca.williams_family
package model

import akka._

import net.liftweb.common._
import Box._
import net.liftweb.util.Helpers._
import net.liftweb.json._
import JsonAST._
import JsonDSL._
import JsonParser._
import Serialization.{read, write}
import org.apache.commons.math.util.MathUtils

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

object R {
  val common: Map[(Int, Int), Ratio] =
    List(1, 2, 4, 8, 15, 30, 60, 125, 250, 500, 1000).map(d => ((1, d), Ratio(1, d))).toMap

  def apply(n: Int = 1, d: Int = 1): Ratio = common.get((n, d)).getOrElse(Ratio(n,d))

  def apply(in: String): Ratio =
    in.split("/").toList match {
      case n :: d :: Nil => R(n.toInt,d.toInt)
      case n :: Nil => R(n.toInt)
      case _ => R(0)
    }

  def apply(in: Double): Ratio = {
    R((in * 1000).toInt, 1000)
  }

  object Implicits {
    implicit def ratioToJValue(in: Ratio): JValue = JArray(List(JInt(in.n), JInt(in.d)))
  }
}

object Ratio {
  def apply(n: Int = 1, d: Int = 1): Ratio = {
    val m = if (d < 0) (-1) else 1
    val g = if (n == 1 || d == 1) (1) else (MathUtils.gcd(n, d))
      if (g == 0) (new Ratio(0,0)) else (new Ratio(m * n / g, m * d / g))
  }
  def unapply(in: Any): Option[(Int,Int)] = in match {
    case r: Ratio => Some((r.n, r.d))
    case _ => None
  }
  def unapply(in: String): Option[Ratio] = in.split("""\s*/\s*""").toList.map(asInt) match {
    case List(Full(n)) => Some(R(n))
    case List(Full(n),Full(d)) => Some(R(n,d))
    case _ => None
  }
}

class Ratio private (val n: Int, val d: Int) {
  override def toString = if (d > 1) (n + " / " + d) else (n.toString)
    override def hashCode: Int = 37 * (37 * 17 * n) * d
  override def equals(in: Any): Boolean = in match {
    case Ratio(a,b) if n == a && d == b => true
    case _ => false
  }
}

object RatioSerializer extends Serializer[Ratio] {
  private val RatioClass = classOf[Ratio]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Ratio] = {
    case (TypeInfo(RatioClass, _), json) => json match {
      case JString(Ratio(r)) => r
      case x => throw new MappingException("Can't convert "+x+" to Ratio")
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: Ratio => JString(x.toString)
  }
}

