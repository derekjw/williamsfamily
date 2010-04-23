package ca.williams_family
package akka

import net.liftweb.common._
import Box._

import model._

import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.config.ScalaConfig._
import se.scalablesolutions.akka.config.OneForOneStrategy

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

abstract class PhotoService extends Actor with Logger {
  faultHandler = Some(OneForOneStrategy(5, 5000))
  trapExit = List(classOf[Exception])

  val storage: PhotoStorage

  implicit val formats = Serialization.formats(NoTypeHints)

  def countPhotos = ((this !! CountPhotos) ?~ "Timed out").asA[java.lang.Integer].map(_.intValue)
  def setPhoto(photo: Photo) = this ! SetPhoto(photo.id,write(photo))
  def getPhoto(id: String) =
    for {
      res <- ((this !! GetPhoto(id)) ?~ "Timed out" ~> 500).asA[Option[String]] ?~ "Invalid response" ~> 500
      json <- res ?~ "Photo not found" ~> 404
    } yield read[Photo](json)

  def receive = {
    case CountPhotos => storage forward CountPhotos
    case msg: SetPhoto => storage ! msg
    case msg: GetPhoto => storage forward msg
  }

  override def shutdown = {
    unlink(storage)
    storage.stop
  }

}
