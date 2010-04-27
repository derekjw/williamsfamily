package ca.williams_family
package akka

import collection.SortedSet

import net.liftweb.common._
import Box._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.dispatch._
import se.scalablesolutions.akka.config.ScalaConfig._
import se.scalablesolutions.akka.config.OneForOneStrategy
//import se.scalablesolutions.akka.stm.Transaction.Local._

import net.liftweb.json._

abstract class PhotoService extends Transactor with Logger {
  faultHandler = Some(OneForOneStrategy(5, 5000))
  trapExit = List(classOf[Exception])

  val storage: PhotoStorage

  protected var indexes: Set[PhotoIndex] = Set()

  def registerIndex(index: PhotoIndex) {
    startLink(index)
    indexes += index
  }

  def countPhotos = ((this !! CountPhotos) ?~ "Timed out").asA[java.lang.Integer].map(_.intValue)

  def setPhoto(photo: Photo) = this ! SetPhoto(photo,None)

  def getPhoto(id: String) =
    for {
      res <- ((this !! GetPhoto(id)) ?~ "Timed out" ~> 500).asA[Option[String]] ?~ "Invalid response" ~> 500
      json <- res ?~ "Photo not found" ~> 404
    } yield Photo.deserialize(json)

  def getPhotosByDate(key: Int): Box[SortedSet[String]] =
    for {
      res <- ((this !! GetPhotosByDate(key)) ?~ "Timed out").asA[SortedSet[String]] ?~ "Invalid Response"
    } yield res

  def receive = {
    case CountPhotos => storage forward CountPhotos
    case msg: SetPhoto => {
      storage ! msg
      indexes.foreach(_ ! msg)
    }
    case msg: GetPhoto => storage forward msg
    case msg: GetPhotos => storage forward msg
    case msg: GetPhotosByDate => {
      val idx = indexes.find{
        case i: PhotoDateIndex => true
        case _ => false
      }
      Box(idx).foreach(_ forward msg)
      if (idx.isEmpty) reply(Failure("Index not found"))
    }
  }

  override def shutdown = {
    unlink(storage)
    storage.stop
    indexes.foreach {i =>
      unlink(i)
      i.stop
    }
  }

}
